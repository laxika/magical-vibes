package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

/** Back face of Dennick, Pious Apprentice. */
public class DennickPiousApparition extends Card {

    public DennickPiousApparition() {
        // Whenever one or more creature cards are put into graveyards from anywhere, investigate.
        // This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                investigateOncePerTurn());
        addEffect(EffectSlot.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                investigateOncePerTurn());

        // If Dennick would be put into a graveyard from anywhere, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }

    private OncePerTurnTriggerEffect investigateOncePerTurn() {
        return new OncePerTurnTriggerEffect(CreateTokenEffect.ofClueToken(1));
    }
}
