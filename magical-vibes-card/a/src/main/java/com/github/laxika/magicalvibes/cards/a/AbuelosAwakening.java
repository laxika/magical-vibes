package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "1")
@CardRegistration(set = "LCI", collectorNumber = "353")
public class AbuelosAwakening extends Card {

    public AbuelosAwakening() {
        CardPredicate artifactOrNonAuraEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardNotPredicate(new CardIsAuraPredicate())))));

        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(artifactOrNonAuraEnchantment)
                .targetGraveyard(true)
                .battlefieldEffectGrants(List.of(
                        new GrantCardTypeEffect(CardType.CREATURE, GrantScope.TARGET),
                        new GrantSubtypeEffect(CardSubtype.SPIRIT, GrantScope.TARGET),
                        SetBasePowerToughnessEffect.indefinitely(1, 1),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, GrantDuration.INDEFINITE)))
                .build());
        addSpellEffect(new PutCounterOnReferencedPermanentEffect(
                PermanentReference.RETURNED, CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
