package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalIfEventValueAtLeastEffect;

@CardRegistration(set = "AFR", collectorNumber = "193")
public class LongRest extends Card {

    public LongRest() {
        setMultiTargetConstraint(MultiTargetConstraint.DIFFERENT_MANA_VALUES);
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(null, 0, true)
                .withReturnedCount());
        addEffect(EffectSlot.SPELL,
                new SetLifeTotalIfEventValueAtLeastEffect(8, GameData.STARTING_LIFE_TOTAL));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
