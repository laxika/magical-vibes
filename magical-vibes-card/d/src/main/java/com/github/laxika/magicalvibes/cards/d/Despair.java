package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

/**
 * Despair — back half of Driven // Despair.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Until end of turn, creatures
 * you control gain menace and "Whenever this creature deals combat damage to a player, that player
 * discards a card."
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Despair extends Card {

    public Despair() {
        // Until end of turn, creatures you control gain menace and
        // "Whenever this creature deals combat damage to a player, that player discards a card."
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SPELL, new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{1}{B}"));
    }
}
