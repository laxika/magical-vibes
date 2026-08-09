package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "27")
public class AetherBarrier extends Card {

    public AetherBarrier() {
        // Whenever a player casts a creature spell, that player sacrifices a permanent of their
        // choice unless they pay {1}.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new MayPayManaEffect(
                        "{1}",
                        null,
                        "Pay {1} or sacrifice a permanent?",
                        MayPayPayer.ENCHANTED_CONTROLLER,
                        new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.TARGET_PLAYER),
                        0))));
    }
}
