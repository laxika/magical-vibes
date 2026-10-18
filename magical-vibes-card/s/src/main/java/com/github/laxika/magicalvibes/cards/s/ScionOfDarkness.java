package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "79")
@CardRegistration(set = "2X2", collectorNumber = "86")
@CardRegistration(set = "ARC", collectorNumber = "23")
public class ScionOfDarkness extends Card {

    public ScionOfDarkness() {
        // The target is chosen as the trigger goes on the stack; "you may" is asked on resolution.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        false, new CardTypePredicate(CardType.CREATURE), false),
                        "Put target creature card from that player's graveyard onto the battlefield under your control?"));
        addCycling("{3}");
    }
}
