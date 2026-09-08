package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "108")
public class SheoldredsEdict extends Card {

    public SheoldredsEdict() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices a nontoken creature of their choice",
                        new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                                SacrificeRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices a creature token of their choice",
                        new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTokenPredicate())),
                                SacrificeRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices a planeswalker of their choice",
                        new SacrificePermanentsEffect(1, new PermanentIsPlaneswalkerPredicate(),
                                SacrificeRecipient.EACH_OPPONENT))
        )));
    }
}
