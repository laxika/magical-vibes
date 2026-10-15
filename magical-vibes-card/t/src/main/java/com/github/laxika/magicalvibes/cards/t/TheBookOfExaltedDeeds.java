package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "4")
public class TheBookOfExaltedDeeds extends Card {

    public TheBookOfExaltedDeeds() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(3),
                new CreateTokenEffect("Angel", 3, 3, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of())));

        var angel = new PermanentHasSubtypePredicate(CardSubtype.ANGEL);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}{W}",
                List.of(
                        new ExileSelfCost(),
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.ENLIGHTENED, 1, angel),
                        new GrantStaticEffectToTargetEffect(new CantLoseGameEffect())
                ),
                "{W}{W}{W}, {T}, Exile The Book of Exalted Deeds: Put an enlightened counter on target Angel. "
                        + "It gains \"You can't lose the game and your opponents can't win the game.\" "
                        + "Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(angel, "Target must be an Angel"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
