package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndExileFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "79")
public class ImmortalCoil extends Card {

    public ImmortalCoil() {
        // "{T}, Exile two cards from your graveyard: Draw a card."
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new ExileNCardsFromGraveyardCost(2, null), new DrawCardEffect(1)),
                "{T}, Exile two cards from your graveyard: Draw a card."));

        // "If damage would be dealt to you, prevent that damage. Exile a card from your graveyard
        //  for each 1 damage prevented this way."
        addEffect(EffectSlot.STATIC, new PreventAllDamageToControllerAndExileFromGraveyardEffect());

        // "When there are no cards in your graveyard, you lose the game." — state-triggered (CR 603.8).
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    var graveyard = gameData.playerGraveyards.get(controllerId);
                    return graveyard == null || graveyard.isEmpty();
                },
                List.of(new ControllerLosesGameEffect()),
                "Immortal Coil's state-triggered ability"
        ));
    }
}
