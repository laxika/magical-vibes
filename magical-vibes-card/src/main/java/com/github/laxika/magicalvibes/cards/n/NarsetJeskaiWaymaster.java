package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "TDM", collectorNumber = "209")
public class NarsetJeskaiWaymaster extends Card {

    public NarsetJeskaiWaymaster() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new DiscardOwnHandThenDrawEffect(new SpellsCastThisTurn(CountScope.CONTROLLER)),
                "Discard your hand?"));
    }
}
