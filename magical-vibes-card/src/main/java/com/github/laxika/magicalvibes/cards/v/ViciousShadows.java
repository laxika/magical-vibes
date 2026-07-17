package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ALA", collectorNumber = "119")
public class ViciousShadows extends Card {

    public ViciousShadows() {
        // Whenever a creature dies, you may have this enchantment deal damage to target player
        // equal to the number of cards in that player's hand. The "may" and target are resolved on
        // the stack (ON_ANY_CREATURE_DIES pushes non-targeting entries).
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MayEffect(
                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER),
                "Deal damage to target player equal to the number of cards in their hand?"));
    }
}
