package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "186")
public class DanceOfTheManse extends Card {

    public DanceOfTheManse() {
        CardAllOfPredicate nonAuraEnchantment = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardNotPredicate(new CardIsAuraPredicate())));
        CardAnyOfPredicate artifactOrNonAuraEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT), nonAuraEnchantment));
        CardAllOfPredicate eligibleCard = new CardAllOfPredicate(List.of(
                artifactOrNonAuraEnchantment, new CardMaxManaValueXPredicate()));

        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                eligibleCard, new XValue()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(6), new AnimateReturnedPermanentsEffect(
                new AnimatePermanentsEffect(4, 4, List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.PERMANENT))));
    }
}
