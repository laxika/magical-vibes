package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealEachDrawEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.amount.Fixed;

@CardRegistration(set = "OHOP", collectorNumber = "33")
public class SeaOfSand extends Card {

    public SeaOfSand() {
        CardEffect landDraw = new GainLifeEffect(new Fixed(3), GainLifeRecipient.TRIGGERING_PLAYER);
        CardEffect nonlandDraw = new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER);
        RevealEachDrawEffect drawEffect = new RevealEachDrawEffect(landDraw, nonlandDraw);

        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, drawEffect);
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, drawEffect);
        target(TargetFilters.permanent()).addEffect(
                EffectSlot.CHAOS_TRIGGERED, new PutTargetOnTopOfLibraryEffect());
    }
}
