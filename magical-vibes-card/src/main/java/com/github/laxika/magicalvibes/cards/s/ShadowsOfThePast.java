package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "118")
public class ShadowsOfThePast extends Card {

    public ShadowsOfThePast() {
        // Whenever a creature dies, scry 1.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new ScryEffect(1));

        // {4}{B}: Each opponent loses 2 life and you gain 2 life.
        // Activate only if there are four or more creature cards in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT), new GainLifeEffect(2)),
                "{4}{B}: Each opponent loses 2 life and you gain 2 life. Activate only if there are "
                        + "four or more creature cards in your graveyard.")
                .withRequiredGraveyardCards(new CardTypePredicate(CardType.CREATURE), 4, "creature cards in your graveyard"));
    }
}
