package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "150")
public class RanAndShaw extends Card {

    public RanAndShaw() {
        var dragonOrLesson = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.DRAGON),
                new CardSubtypePredicate(CardSubtype.LESSON)));
        // "When Ran and Shaw enter, if you cast them and there are three or more Dragon and/or Lesson
        // cards in your graveyard, create a token that's a copy of Ran and Shaw, except it's not legendary."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new AllOf(List.of(new WasCast(), new GraveyardCardThreshold(3, dragonOrLesson))),
                new CreateTokenCopyOfSourceEffect(true, 1), true));

        // Firebending 2.
        addEffect(EffectSlot.ON_ATTACK, new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new BoostAllOwnCreaturesEffect(2, 0,
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                "{3}{R}: Dragons you control get +2/+0 until end of turn."
        ));
    }
}
