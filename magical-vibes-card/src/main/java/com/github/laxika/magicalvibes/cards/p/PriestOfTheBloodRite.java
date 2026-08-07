package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "112")
public class PriestOfTheBloodRite extends Card {

    public PriestOfTheBloodRite() {
        // When this creature enters, create a 5/5 black Demon creature token with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Demon", 5, 5, CardColor.BLACK,
                        List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of()));

        // At the beginning of your upkeep, you lose 2 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(2));
    }
}
