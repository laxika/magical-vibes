package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect;

@CardRegistration(set = "SLD", collectorNumber = "347")
public class WernogRidersChaplain extends Card {

    public WernogRidersChaplain() {
        EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect effect =
                new EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, effect);
    }
}
