package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GraveyardOwner;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTopCreatureCardFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "WTH", collectorNumber = "62")
public class BoneDancer extends Card {

    public BoneDancer() {
        // Whenever this creature attacks and isn't blocked, you may put the top creature card of
        // defending player's graveyard onto the battlefield under your control. If you do, this
        // creature assigns no combat damage this turn. The trigger bakes the defending player as
        // targetId; the "if you do" clause only applies when a creature card was actually returned,
        // so both halves live in one effect.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new ReturnTopCreatureCardFromGraveyardToBattlefieldEffect(
                        GraveyardOwner.TARGET_PLAYER, true),
                        "You may put the top creature card of defending player's graveyard onto the battlefield under your control. If you do, this creature assigns no combat damage this turn."));
    }
}
