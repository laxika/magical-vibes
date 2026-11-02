package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "112")
public class UnifyingTheory extends Card {

    public UnifyingTheory() {
        // Whenever a player casts a spell, that player may pay {2}. If the player does, they draw a card.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new MayPayManaEffect("{2}", new DrawCardForTargetPlayerEffect(1),
                        "Pay {2} to draw a card?", MayPayPayer.TRIGGERING_PLAYER))));
    }
}
