package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "57")
public class BogElemental extends Card {

    public BogElemental() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsLandPredicate(), "a land"));
    }
}
