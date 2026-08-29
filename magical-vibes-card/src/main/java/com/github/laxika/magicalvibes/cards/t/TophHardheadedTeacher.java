package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "246")
public class TophHardheadedTeacher extends Card {

    public TophHardheadedTeacher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))))
                                .targetGraveyard(true)
                                .build(),
                        "a card"),
                "Discard a card to return target instant or sorcery card from your graveyard to your hand?"));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.LESSON)),
                List.of(new EarthbendTargetLandEffect(1)),
                null,
                TargetFilters.landYouControl()));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.LESSON),
                List.of(new EarthbendTargetLandEffect(2)),
                null,
                TargetFilters.landYouControl()));
    }
}
