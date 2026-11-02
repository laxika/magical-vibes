package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "177")
public class BoseijuReachesSkyward extends Card {

    public BoseijuReachesSkyward() {
        setBackFaceCard(new BranchOfBoseiju());

        addTrigger(Triggers.chapter(1), new SearchLibraryEffect(
                new Fixed(2),
                new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.BASIC),
                        new CardSubtypePredicate(CardSubtype.FOREST))),
                LibrarySearchDestination.HAND));
        addTrigger(Triggers.chapter(2), new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(
                new CardTypePredicate(CardType.LAND), 1));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "BranchOfBoseiju";
    }
}
