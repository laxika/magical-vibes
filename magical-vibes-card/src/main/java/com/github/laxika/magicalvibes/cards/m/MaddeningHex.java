package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToRandomOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;

import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "70")
public class MaddeningHex extends Card {

    public MaddeningHex() {
        AttachSourceAuraToRandomOpponentEffect reattach = new AttachSourceAuraToRandomOpponentEffect();
        RollD6Effect roll = new RollD6Effect(List.of(
                SequenceEffect.of(new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PLAYER), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(2, DamageRecipient.ENCHANTED_PLAYER), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(3, DamageRecipient.ENCHANTED_PLAYER), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(4, DamageRecipient.ENCHANTED_PLAYER), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(5, DamageRecipient.ENCHANTED_PLAYER), reattach),
                SequenceEffect.of(new DealDamageToPlayersEffect(6, DamageRecipient.ENCHANTED_PLAYER), reattach)));

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(roll),
                new StackEntryControlledByEnchantedPlayerPredicate()));
    }
}
