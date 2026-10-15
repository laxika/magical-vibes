package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "25")
public class RoadsGoEverEverOn extends Card {

    public RoadsGoEverEverOn() {
        CardAllOfPredicate basicPlains = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardTypePredicate(CardType.LAND),
                new CardSubtypePredicate(CardSubtype.PLAINS)));
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new SequenceEffect(List.of(
                        new SearchLibraryForCardsToExileWithSourceEffect(basicPlains, 2),
                        new GainLifeEffect(2))));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new PutCardExiledWithSourceIntoHandEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new PutCardExiledWithSourceIntoHandEffect());

        PermanentCount plainsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS), CountScope.CONTROLLER);
        PermanentAllOfPredicate creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        addEffect(EffectSlot.SAGA_CHAPTER_IV,
                new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new BoostTargetCreatureEffect(plainsYouControl, plainsYouControl,
                                creatureYouControl, GrantDuration.END_OF_TURN)));
    }
}
