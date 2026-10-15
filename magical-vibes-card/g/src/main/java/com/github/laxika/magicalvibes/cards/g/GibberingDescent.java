package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "FUT", collectorNumber = "66")
public class GibberingDescent extends Card {

    public GibberingDescent() {
        addCastingOption(new MadnessCast("{2}{B}{B}"));
        addEffect(EffectSlot.STATIC, PlayersSkipUpkeepStepEffect.controllerWithEmptyHand());
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.ACTIVE_PLAYER),
                new DiscardEffect(1, DiscardRecipient.ACTIVE_PLAYER)));
    }
}
