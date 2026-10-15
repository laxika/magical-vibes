package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenPermanentYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "26")
@CardRegistration(set = "FIC", collectorNumber = "194")
public class SummonGoodKingMogXII extends Card {

    public SummonGoodKingMogXII() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                2, "Moogle", 1, 2, CardColor.WHITE, List.of(CardSubtype.MOOGLE),
                Set.of(Keyword.LIFELINK), Set.of()));

        PermanentPredicate nonSagaToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SAGA))
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                null,
                List.of(new CreateTokenCopyOfChosenPermanentYouControlEffect(nonSagaToken)),
                false,
                false,
                null,
                false));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                null,
                List.of(new CreateTokenCopyOfChosenPermanentYouControlEffect(nonSagaToken)),
                false,
                false,
                null,
                false));

        PermanentPredicate otherMoogle = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.MOOGLE),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new PutCounterOnEachControlledPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2, otherMoogle));
    }
}
