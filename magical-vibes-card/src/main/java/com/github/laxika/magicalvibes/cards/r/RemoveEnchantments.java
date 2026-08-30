package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToAttackingCreatureControlledByOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "33")
public class RemoveEnchantments extends Card {

    public RemoveEnchantments() {
        PermanentPredicate ownedAndControlledEnchantments = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentOwnedBySourceControllerPredicate()
        ));
        PermanentPredicate ownedAurasOnControlledPermanents = new PermanentAllOfPredicate(List.of(
                new PermanentOwnedBySourceControllerPredicate(),
                new PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate()
        ));
        PermanentPredicate ownedAurasOnAttackingOpponentCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentOwnedBySourceControllerPredicate(),
                new PermanentIsAuraAttachedToAttackingCreatureControlledByOpponentOfSourceControllerPredicate()
        ));
        PermanentPredicate returnFilter = new PermanentAnyOfPredicate(List.of(
                ownedAndControlledEnchantments,
                ownedAurasOnControlledPermanents,
                ownedAurasOnAttackingOpponentCreatures
        ));

        PermanentPredicate controlledEnchantments = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentControlledBySourceControllerPredicate()
        ));
        PermanentPredicate aurasOnControlledPermanents =
                new PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate();
        PermanentPredicate aurasOnAttackingOpponentCreatures =
                new PermanentIsAuraAttachedToAttackingCreatureControlledByOpponentOfSourceControllerPredicate();
        PermanentPredicate destroyFilter = new PermanentAnyOfPredicate(List.of(
                controlledEnchantments,
                aurasOnControlledPermanents,
                aurasOnAttackingOpponentCreatures
        ));

        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                ReturnToHandEffect.allPermanentsMatching(returnFilter),
                new DestroyAllPermanentsEffect(destroyFilter)
        ));
    }
}
