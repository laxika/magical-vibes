package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "122")
public class SilversmoteGhoul extends Card {

    public SilversmoteGhoul() {
        // At the beginning of your end step, if you gained 3 or more life this turn, return this
        // card from your graveyard to the battlefield tapped.
        addEffect(EffectSlot.GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(3), new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)));

        // {1}{B}, Sacrifice this creature: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{1}{B}, Sacrifice this creature: Draw a card."
        ));
    }
}
