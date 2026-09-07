package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOwnPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "182")
public class DisplayOfDominance extends Card {

    public DisplayOfDominance() {
        Set<CardColor> colors = Set.of(CardColor.BLUE, CardColor.BLACK);
        PermanentPredicate blueOrBlackNoncreature = new PermanentAllOfPredicate(List.of(
                new PermanentColorInPredicate(colors),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target blue or black noncreature permanent",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                blueOrBlackNoncreature,
                                "Target must be a blue or black noncreature permanent.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Permanents you control can't be the targets of blue or black spells your opponents control this turn",
                        new GrantStaticEffectToOwnPermanentsUntilEndOfTurnEffect(
                                TargetingRestrictionEffect.fromOpponentSpellColors(colors)))
        )));
    }
}
