package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayInvestigateEffect;

@CardRegistration(set = "SLX", collectorNumber = "8")
public class WernogRidersChaplain extends Card {

    public WernogRidersChaplain() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachOpponentMayInvestigateEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new EachOpponentMayInvestigateEffect());
    }
}
