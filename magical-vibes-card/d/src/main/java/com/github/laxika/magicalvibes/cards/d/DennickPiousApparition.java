package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of Dennick, Pious Apprentice. */
public class DennickPiousApparition extends Card {

    public DennickPiousApparition() {
        // Whenever one or more creature cards are put into graveyards from anywhere, investigate.
        // This ability triggers only once each turn.
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_OWN),
                investigateOncePerTurn());
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS),
                investigateOncePerTurn());

        // If Dennick would be put into a graveyard from anywhere, exile it instead.
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }

    private OncePerTurnTriggerEffect investigateOncePerTurn() {
        return new OncePerTurnTriggerEffect(CreateTokenEffect.ofClueToken(1));
    }
}
