package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithTriggeringSpellPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "121")
public class CabalShrine extends Card {

    public CabalShrine() {
        // Whenever a player casts a spell, that player discards X cards, where X is the number of
        // cards in all graveyards with the same name as that spell.
        addTrigger(Triggers.playerCastsSpell().anySpell(), new DiscardEffect(
                new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER),
                DiscardRecipient.TRIGGERING_PLAYER));
    }
}
