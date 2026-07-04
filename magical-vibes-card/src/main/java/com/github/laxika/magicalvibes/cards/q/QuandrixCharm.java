package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "217")
@CardRegistration(set = "SOS", collectorNumber = "365")
public class QuandrixCharm extends Card {

    public QuandrixCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {2}",
                        new CounterUnlessPaysEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsEnchantmentPredicate(),
                                "Target must be an enchantment.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature has base power and toughness 5/5 until end of turn",
                        new SetBasePowerToughnessUntilEndOfTurnEffect(5, 5),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."))
        )));
    }
}
