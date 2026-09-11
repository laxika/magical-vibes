package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NezumiRoadCaptain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "NEO", collectorNumber = "117")
public class OkibaReckonerRaid extends Card {

    public OkibaReckonerRaid() {
        setBackFaceCard(new NezumiRoadCaptain());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GainLifeEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "NezumiRoadCaptain";
    }
}
