package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "67")
public class PowerOfPersuasion extends Card {

    public PowerOfPersuasion() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new RollD20Effect(
                        new ReturnTargetSpellOrCreatureToHandEffect(),
                        new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect(),
                        new GainControlOfTargetEffect(ControlDuration.UNTIL_END_OF_YOUR_NEXT_TURN)));
    }
}
