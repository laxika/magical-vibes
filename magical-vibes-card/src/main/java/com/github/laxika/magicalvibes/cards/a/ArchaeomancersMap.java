package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsMoreLandsThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "230")
@CardRegistration(set = "SOC", collectorNumber = "136")
@CardRegistration(set = "C21", collectorNumber = "12")
public class ArchaeomancersMap extends Card {

    public ArchaeomancersMap() {
        CardPredicate basicPlains = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardTypePredicate(CardType.LAND),
                new CardSubtypePredicate(CardSubtype.PLAINS)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(new Fixed(2), basicPlains, LibrarySearchDestination.HAND));

        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new ConditionalEffect(
                        new TargetPlayerControlsMoreLandsThanController(),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?")));
    }
}
