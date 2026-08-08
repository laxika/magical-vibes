package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "99")
public class RuricTharTheUnbowed extends Card {

    public RuricTharTheUnbowed() {
        // Vigilance and reach are auto-loaded as keywords from Scryfall.

        // Ruric Thar attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // Whenever a player casts a noncreature spell, Ruric Thar deals 6 damage to that player.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new DealDamageToPlayersEffect(6, DamageRecipient.TRIGGERING_PLAYER))));
    }
}
