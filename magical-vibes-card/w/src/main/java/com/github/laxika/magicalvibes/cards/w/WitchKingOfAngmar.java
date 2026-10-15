package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BatchedCombatDamageToYouTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtCombatDamageToSourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "20")
@CardRegistration(set = "HOC", collectorNumber = "60")
public class WitchKingOfAngmar extends Card {

    public WitchKingOfAngmar() {
        PermanentPredicate creatureThatDealtCombatDamage = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentDealtCombatDamageToSourceControllerThisTurnPredicate()));

        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
                new BatchedCombatDamageToYouTriggerEffect(SequenceEffect.of(
                        new SacrificePermanentsEffect(
                                1, creatureThatDealtCombatDamage, SacrificeRecipient.EACH_OPPONENT),
                        new RingTemptsYouEffect())));

        // Discard a card: Witch-king of Angmar gains indestructible until end of turn. Tap him.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF)),
                "Discard a card: Witch-king of Angmar gains indestructible until end of turn. Tap him."));
    }
}
