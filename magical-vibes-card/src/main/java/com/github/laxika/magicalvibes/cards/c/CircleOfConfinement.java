package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesNameWithCardExiledWithSourcePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "7")
public class CircleOfConfinement extends Card {

    private static final PermanentPredicate TARGET_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
            new PermanentMaxManaValuePredicate(3)
    ));

    public CircleOfConfinement() {
        target(new PermanentPredicateTargetFilter(
                TARGET_CREATURE,
                "Target must be a creature an opponent controls with mana value 3 or less"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentUntilSourceLeavesEffect(false, TARGET_CREATURE));

        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.VAMPIRE),
                List.of(new GainLifeEffect(2)),
                new StackEntrySharesNameWithCardExiledWithSourcePredicate()
        ));
    }
}
