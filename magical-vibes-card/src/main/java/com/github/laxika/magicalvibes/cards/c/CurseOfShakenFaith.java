package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "134")
public class CurseOfShakenFaith extends Card {

    public CurseOfShakenFaith() {
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(2, DamageRecipient.ENCHANTED_PLAYER);

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(damage),
                null,
                null,
                new StackEntryControlledByEnchantedPlayerPredicate(),
                false,
                false,
                null,
                2));

        SpellCopyTriggerEffect copyTrigger = new SpellCopyTriggerEffect(
                null,
                List.of(damage),
                new StackEntryControlledByEnchantedPlayerPredicate(),
                true);
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL, copyTrigger);
        addEffect(EffectSlot.ON_OPPONENT_COPIES_SPELL, copyTrigger);
    }
}
