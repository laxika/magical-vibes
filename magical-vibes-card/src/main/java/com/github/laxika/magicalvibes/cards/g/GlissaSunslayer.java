package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "202")
public class GlissaSunslayer extends Card {

    public GlissaSunslayer() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You draw a card and lose 1 life",
                        List.of(new DrawCardEffect(1), new LoseLifeEffect(1))),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        new DestroyTargetPermanentEffect(), TargetFilters.enchantment()),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove up to three counters from target permanent",
                        new RemoveChosenCountersFromTargetPermanentEffect(3), TargetFilters.permanent())
        )));
    }
}
