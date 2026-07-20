package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AKH", collectorNumber = "64")
public class OpenIntoWonder extends Card {

    public OpenIntoWonder() {
        // X target creatures can't be blocked this turn. Until end of turn, those creatures gain
        // "Whenever this creature deals combat damage to a player, draw a card."
        // Single X-scaled creature target group: each targeted creature is made unblockable and
        // granted the temporary combat-damage-to-player draw trigger until end of turn.
        targetX(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures"
        ), 100)
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect())
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1)));
    }
}
