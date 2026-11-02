package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
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
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "25")
public class RoadsGoEverEverOn extends Card {

    public RoadsGoEverEverOn() {
        CardAllOfPredicate basicPlains = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardTypePredicate(CardType.LAND),
                new CardSubtypePredicate(CardSubtype.PLAINS)));
        addTrigger(Triggers.chapter(1),
                new SequenceEffect(List.of(
                        new SearchLibraryForCardsToExileWithSourceEffect(basicPlains, 2),
                        new GainLifeEffect(2))));

        addTrigger(Triggers.chapter(2), PutCardExiledWithSourceIntoHandEffect.returningToOwner());
        addTrigger(Triggers.chapter(3), PutCardExiledWithSourceIntoHandEffect.returningToOwner());

        PermanentCount plainsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS), CountScope.CONTROLLER);
        PermanentAllOfPredicate creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        addTrigger(Triggers.chapter(4),
                new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.youAttack(),
                        new BoostTargetCreatureEffect(plainsYouControl, plainsYouControl,
                                creatureYouControl, GrantDuration.END_OF_TURN))));
    }
}
