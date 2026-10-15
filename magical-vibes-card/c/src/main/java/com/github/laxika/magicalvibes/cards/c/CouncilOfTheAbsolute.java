package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForChosenNameSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "62")
public class CouncilOfTheAbsolute extends Card {

    public CouncilOfTheAbsolute() {
        // As this creature enters, choose a noncreature, nonland card name.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardNameOnEnterEffect(List.of(CardType.CREATURE, CardType.LAND)));
        // Your opponents can't cast spells with the chosen name.
        addEffect(EffectSlot.STATIC, new SpellsWithChosenNameCantBeCastEffect(true));
        // Spells with the chosen name you cast cost {2} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForChosenNameSpellsEffect(2));
    }
}
