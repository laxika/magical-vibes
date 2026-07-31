package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "103")
public class LilianasReaver extends Card {

    public LilianasReaver() {
        // Whenever this creature deals combat damage to a player, that player discards a card and
        // you create a tapped 2/2 black Zombie creature token. One atomic entry so both halves
        // resolve in oracle order; CombatDamageService bakes the damaged player in as the targetId
        // the discard reads.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                new CreateTokenEffect(1, "Zombie", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true)));
    }
}
