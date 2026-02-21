package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

public record AddManaOnEnchantedLandTapEffect(ManaColor color, int amount) implements CardEffect {
}
