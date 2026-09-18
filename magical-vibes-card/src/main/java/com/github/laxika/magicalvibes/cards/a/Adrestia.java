package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceWasCrewedBySubtypeThisTurn;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "68")
public class Adrestia extends Card {

    public Adrestia() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceWasCrewedBySubtypeThisTurn(CardSubtype.ASSASSIN),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.ASSASSIN))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
