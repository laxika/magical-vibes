package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
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
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "1")
@CardRegistration(set = "LCI", collectorNumber = "353")
public class AbuelosAwakening extends Card {

    public AbuelosAwakening() {
        CardPredicate artifactOrNonAuraEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardNotPredicate(new CardIsAuraPredicate())))));

        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(artifactOrNonAuraEnchantment)
                .targetGraveyard(true)
                .grantSubtype(CardSubtype.SPIRIT)
                .build());
        addEffect(EffectSlot.SPELL, new PutCounterOnReferencedPermanentEffect(
                PermanentReference.RETURNED, CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addEffect(EffectSlot.SPELL, new AnimateReturnedPermanentIfSubtypeEffect(
                CardSubtype.SPIRIT,
                new AnimatePermanentsEffect(1, 1, List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING),
                        null, Set.of(), GrantScope.TARGET, EffectDuration.PERMANENT)));
    }
}
