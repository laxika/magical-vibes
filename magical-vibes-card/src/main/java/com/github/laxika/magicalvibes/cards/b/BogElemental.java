package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "57")
public class BogElemental extends Card {

    public BogElemental() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsLandPredicate(), "a land"));
    }
}
