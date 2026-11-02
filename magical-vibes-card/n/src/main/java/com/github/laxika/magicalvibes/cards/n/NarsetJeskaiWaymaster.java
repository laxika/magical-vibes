package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "209")
public class NarsetJeskaiWaymaster extends Card {

    public NarsetJeskaiWaymaster() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                new DiscardOwnHandThenDrawEffect(new SpellsCastThisTurn(CountScope.CONTROLLER)),
                "Discard your hand?"));
    }
}
