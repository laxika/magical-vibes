package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.FaceDownCreatureEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "180")
public class TunnelTipster extends Card {

    public TunnelTipster() {
        // At the beginning of your end step, if a face-down creature entered the battlefield under
        // your control this turn, put a +1/+1 counter on this creature.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new FaceDownCreatureEnteredThisTurn(),
                        new PutCountersOnSourceEffect(1, 1, 1)));

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
