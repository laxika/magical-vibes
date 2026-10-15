package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "92")
@CardRegistration(set = "PIP", collectorNumber = "407")
@CardRegistration(set = "PIP", collectorNumber = "620")
@CardRegistration(set = "PIP", collectorNumber = "935")
public class ArcadeGannon extends Card {

    public ArcadeGannon() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DrawCardEffect(),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new PutCountersOnSelfEffect(CounterType.QUEST)),
                "{T}: Draw a card, then discard a card. Put a quest counter on Arcade Gannon."));

        addEffect(EffectSlot.STATIC, new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardSubtypePredicate(CardSubtype.HUMAN))),
                        new CardManaValueAtMostSourceCountersPredicate(CounterType.QUEST)))));
    }
}
