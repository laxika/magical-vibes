package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "599")
public class SpectroxMines extends Card {

    public SpectroxMines() {
        SequenceEffect arrivalAndUpkeep = SequenceEffect.of(
                new LoseLifeEffect(3),
                CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, arrivalAndUpkeep);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, arrivalAndUpkeep);

        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                CreateTokenEffect.ofFoodToken(1),
                new CreateTokenEffect(
                        "Human Rogue", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.HUMAN, CardSubtype.ROGUE), Set.of(), Set.of())));
    }
}
