package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "99")
@CardRegistration(set = "GK2", collectorNumber = "80")
@CardRegistration(set = "A25", collectorNumber = "216")
@CardRegistration(set = "2X2", collectorNumber = "271")
@CardRegistration(set = "PIO", collectorNumber = "239")
public class RuricTharTheUnbowed extends Card {

    public RuricTharTheUnbowed() {
        // Vigilance and reach are auto-loaded as keywords from Scryfall.

        // Ruric Thar attacks each combat if able.
        addStaticEffect(new MustAttackEffect());

        // Whenever a player casts a noncreature spell, Ruric Thar deals 6 damage to that player.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new DealDamageToPlayersEffect(6, DamageRecipient.TRIGGERING_PLAYER))));
    }
}
