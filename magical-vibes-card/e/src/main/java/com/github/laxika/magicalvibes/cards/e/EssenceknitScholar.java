package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "187")
public class EssenceknitScholar extends Card {

    public EssenceknitScholar() {
        // When Essenceknit Scholar enters, create a 1/1 black and green Pest creature token with
        // "Whenever this token attacks, you gain 1 life."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE, 1, "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_ATTACK, new GainLifeEffect(1)),
                List.of(), false, false, false, 0, Set.of()));

        // At the beginning of your end step, if a creature died under your control this turn, draw a card.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new CreatureDiedUnderYourControlThisTurn(),
                new DrawCardEffect(1)));
    }
}
