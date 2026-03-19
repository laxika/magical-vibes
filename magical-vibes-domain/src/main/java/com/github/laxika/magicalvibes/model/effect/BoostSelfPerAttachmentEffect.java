package com.github.laxika.magicalvibes.model.effect;

public record BoostSelfPerAttachmentEffect(int power, int toughness, boolean countAuras,
                                           boolean countEquipment) implements CardEffect {
}
