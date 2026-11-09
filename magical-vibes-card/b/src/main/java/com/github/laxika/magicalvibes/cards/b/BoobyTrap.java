package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsChosenPlayer;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenNamePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "289")
@CardRegistration(set = "TMP", collectorNumber = "277")
public class BoobyTrap extends Card {

    public BoobyTrap() {
        // "As this artifact enters, choose an opponent and a card name other than a basic land card name."
        // The chosen opponent is the (single) opponent; the name is stamped onto the permanent.
        addStaticEffect(new ChooseCardNameOnEnterEffect(
                List.of(), ChooseCardNameOnEnterEffect.HandAccess.NONE, false, null, true));
        addStaticEffect(new ChooseOpponentOnEnterEffect());
        // The chosen player reveals each card they draw.
        addStaticEffect(RevealDrawnCardsEffect.eachByChosenPlayer());
        // When the chosen player draws a card with the chosen name, sacrifice this artifact. If you do, this artifact
        // deals 10 damage to that player.
        addTrigger(List.of(Triggers.opponentDrawsCard().withFilter(new CardHasSourceChosenNamePredicate()),
                        Triggers.youDrawCard().withFilter(new CardHasSourceChosenNamePredicate())),
                TriggerOptions.DEFAULT.withEventCondition(new TargetPlayerIsChosenPlayer()),
                new SacrificeSelfThenEffect(new DealDamageToPlayersEffect(10, DamageRecipient.TRIGGERING_PLAYER)));
    }
}
