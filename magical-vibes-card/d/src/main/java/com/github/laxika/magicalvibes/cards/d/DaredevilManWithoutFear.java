package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MSH", collectorNumber = "213")
public class DaredevilManWithoutFear extends Card {

    public DaredevilManWithoutFear() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addTrigger(Triggers.youAttack(), new MayEffect(
                SequenceEffect.of(
                        new ExileTopCardMayPlayThisTurnEffect(false),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardSubtypePredicate(CardSubtype.HERO)),
                                new BoostSelfEffect(2, 1))),
                "Exile the top card of your library?"));
    }
}
