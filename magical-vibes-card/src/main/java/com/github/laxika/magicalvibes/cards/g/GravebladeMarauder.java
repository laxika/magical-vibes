package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "101")
public class GravebladeMarauder extends Card {

    public GravebladeMarauder() {
        // Triggered: whenever this creature deals combat damage to a player, that player loses life
        // equal to the number of creature cards in your graveyard. The damaged player is baked in as
        // the targetId by CombatDamageService, so no target(...) is needed.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LoseLifeEffect(new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
