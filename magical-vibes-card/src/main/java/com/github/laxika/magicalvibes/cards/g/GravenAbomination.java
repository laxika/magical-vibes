package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "HOU", collectorNumber = "162")
public class GravenAbomination extends Card {

    public GravenAbomination() {
        // "Whenever this creature attacks, exile target card from defending player's graveyard."
        // Attack-trigger analogue of Disposal Mummy's ETB / Ruin Rat's death exile. The defending
        // player's graveyard card is chosen as the trigger goes on the stack
        // (CombatAttackService → GraveyardTargetingService.handleAttackGraveyardTargeting).
        addEffect(EffectSlot.ON_ATTACK,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD));
    }
}
