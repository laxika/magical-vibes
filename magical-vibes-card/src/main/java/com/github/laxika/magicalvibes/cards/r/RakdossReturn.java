package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "RTR", collectorNumber = "188")
public class RakdossReturn extends Card {

    public RakdossReturn() {
        // Deals X damage to target opponent or planeswalker; that player or that planeswalker's
        // controller discards X cards. The discard piggybacks on the damage effect's target.
        // AnyTargetPredicateTargetFilter enforces opponent-only players at the card level (using the
        // caster's controller), which the effect-level validator can't for a spell with no permanent source.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker"
        )).addEffect(EffectSlot.SPELL, new DealDamageToTargetOpponentOrPlaneswalkerEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new DiscardEffect(new XValue(), DiscardRecipient.TARGET_PLAYER_OR_PERMANENT_CONTROLLER));
    }
}
