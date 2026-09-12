package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromAnyLibraryTopEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;

@CardRegistration(set = "OPC2", collectorNumber = "39")
public class WindriddlePalaces extends Card {

    public WindriddlePalaces() {
        addEffect(EffectSlot.STATIC, PlayWithTopCardRevealedEffect.forAllPlayers());
        addEffect(EffectSlot.STATIC, new AllowPlayFromAnyLibraryTopEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MillEffect(1, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
