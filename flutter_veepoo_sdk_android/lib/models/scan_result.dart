import 'package:equatable/equatable.dart';

class ScanResult extends Equatable {
  const ScanResult(this.name, this.address);

  final String? name;
  final String? address;

  factory ScanResult.fromMap(Map<String, String> datas) {
    return ScanResult(datas['name'], datas['address']);
  }

  @override
  List<Object?> get props => [name, address];
}
