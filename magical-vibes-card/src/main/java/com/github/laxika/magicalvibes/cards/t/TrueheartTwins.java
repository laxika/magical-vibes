package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "153")
public class TrueheartTwins extends Card {

    public TrueheartTwins() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                "Exert Trueheart Twins as it attacks?"));

        addEffect(EffectSlot.ON_CONTROLLER_EXERTS, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
