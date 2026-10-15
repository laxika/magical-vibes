package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IfWonClashEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "LRW", collectorNumber = "237")
public class SylvanEchoes extends Card {

    public SylvanEchoes() {
        // Whenever you clash and win, you may draw a card.
        //
        // Non-targeting clash trigger: TriggerCollectionService.fireClashTriggers keeps the
        // IfWonClashEffect branch only when the controller won and pushes the wrapped effect
        // straight onto the stack.
        addEffect(EffectSlot.ON_CONTROLLER_CLASHES, new IfWonClashEffect(
                new MayEffect(new DrawCardEffect(1), "Draw a card?")));
    }
}
