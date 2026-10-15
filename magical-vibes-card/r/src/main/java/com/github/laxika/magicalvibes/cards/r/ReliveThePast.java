package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "226")
@CardRegistration(set = "MKM", collectorNumber = "419")
public class ReliveThePast extends Card {

    public ReliveThePast() {
        CardPredicate nonAuraEnchantment = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.AURA))));

        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
                List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.LAND),
                        nonAuraEnchantment),
                List.of(
                        GraveyardChoiceDestination.BATTLEFIELD,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        GraveyardChoiceDestination.BATTLEFIELD),
                List.of("artifact card", "land card", "non-Aura enchantment card")));
        addEffect(EffectSlot.SPELL, new AnimateReturnedPermanentsEffect(
                new AnimatePermanentsEffect(5, 5, List.of(CardSubtype.ELEMENTAL), Set.of(), null,
                        Set.of(), GrantScope.TARGET, EffectDuration.PERMANENT)));
    }
}
