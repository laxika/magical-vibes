package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryMatchesPredicate;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "41")
public class WaterWeird extends Card {

    public WaterWeird() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new ConditionalEffect(
                        new TopCardOfLibraryMatchesPredicate(new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                        new PutCountersOnSourceEffect(1, 1, 1)),
                new ConditionalEffect(
                        new NotCondition(new TopCardOfLibraryMatchesPredicate(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                        new MayEffect(new MillEffect(1, MillRecipient.CONTROLLER), "Mill a card?"))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new BoostSelfEffect(1, -1)),
                "{1}{U}: Water Weird gets +1/-1 until end of turn."));
    }
}
