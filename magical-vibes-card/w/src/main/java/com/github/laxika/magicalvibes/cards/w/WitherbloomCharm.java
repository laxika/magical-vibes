package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "244")
@CardRegistration(set = "SOS", collectorNumber = "367")
public class WitherbloomCharm extends Card {

    public WitherbloomCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You may sacrifice a permanent. If you do, draw two cards",
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentTruePredicate(), new DrawCardEffect(2), "a permanent"),
                                "You may sacrifice a permanent to draw two cards")),
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 5 life",
                        new GainLifeEffect(5)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target nonland permanent with mana value 2 or less",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                        new PermanentMaxManaValuePredicate(2))),
                                "Target must be a nonland permanent with mana value 2 or less."))
        )));
    }
}
