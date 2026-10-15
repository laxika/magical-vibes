package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToLifeGainedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileForEachLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DOM", collectorNumber = "98")
public class LichsMastery extends Card {

    public LichsMastery() {
        // "Hexproof"
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF));

        // "You can't lose the game."
        addEffect(EffectSlot.STATIC, new CantLoseGameEffect());

        // "Whenever you gain life, draw that many cards."
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new DrawCardsEqualToLifeGainedEffect());

        // "Whenever you lose life, for each 1 life you lost, exile a permanent you control
        //  or a card from your hand or graveyard."
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE, new ExileForEachLifeLostEffect());

        // "When Lich's Mastery leaves the battlefield, you lose the game."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ControllerLosesGameOnLeavesEffect());
    }
}
