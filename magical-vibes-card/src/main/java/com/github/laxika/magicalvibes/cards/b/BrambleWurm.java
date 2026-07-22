package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "187")
public class BrambleWurm extends Card {

    public BrambleWurm() {
        // When this creature enters, you gain 5 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(5));

        // {2}{G}, Exile this card from your graveyard: You gain 5 life.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new GainLifeEffect(5)
                ),
                "{2}{G}, Exile this card from your graveyard: You gain 5 life."
        ));
    }
}
