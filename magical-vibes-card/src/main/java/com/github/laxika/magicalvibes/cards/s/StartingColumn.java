package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "244")
public class StartingColumn extends Card {

    public StartingColumn() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(2),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "Max speed — {T}, Sacrifice this artifact: Draw two cards, then discard a card."
        ).withActivationCondition(new MaxSpeed(), "Activate only if you have max speed"));
    }
}
