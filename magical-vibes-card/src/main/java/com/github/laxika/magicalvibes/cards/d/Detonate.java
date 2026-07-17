package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Detonate — {X}{R} Sorcery.
 * Destroy target artifact with mana value X. It can't be regenerated. Detonate deals X damage
 * to that artifact's controller.
 */
@CardRegistration(set = "5ED", collectorNumber = "218")
public class Detonate extends Card {

    public Detonate() {
        // Deal the damage while the artifact is still on the battlefield so its controller resolves,
        // then destroy it (can't be regenerated). Both X values are the spell's chosen X.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentManaValueEqualsXPredicate())),
                "Target must be an artifact with mana value X."
        ))
                .addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                        new XValue(), DamageRecipient.TARGET_PERMANENT_CONTROLLER))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(true));
    }
}
