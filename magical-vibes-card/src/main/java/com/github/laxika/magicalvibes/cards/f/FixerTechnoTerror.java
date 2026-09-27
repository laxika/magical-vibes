package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "656")
public class FixerTechnoTerror extends Card {

    public FixerTechnoTerror() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(2), new DrawCardEffect()),
                "{T}, Pay 2 life: Draw a card. Activate only if an artifact entered the battlefield under your control this turn."
        ).withActivationCondition(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT), 1),
                "Activate only if an artifact entered the battlefield under your control this turn."
        ));
    }
}
