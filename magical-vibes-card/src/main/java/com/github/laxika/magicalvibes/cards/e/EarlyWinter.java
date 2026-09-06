package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesPermanentExileEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "93")
public class EarlyWinter extends Card {

    public EarlyWinter() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent exiles an enchantment they control",
                        new TargetPlayerChoosesPermanentExileEffect(
                                new PermanentIsEnchantmentPredicate(), "enchantment"))
        )));
    }
}
