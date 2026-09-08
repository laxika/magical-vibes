package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsFewerCreaturesThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "6")
public class EvenTheOdds extends Card {

    public EvenTheOdds() {
        setCastCondition(new ControllerControlsFewerCreaturesThanEachOpponent());
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Soldier", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
