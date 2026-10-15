package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealMatchingCardsFromTargetHandAndKeepEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryForOwnerOfCardExiledWithSourceBelowManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.TrackChosenCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "YMKM", collectorNumber = "9")
public class CacklingObserver extends Card {

    public CacklingObserver() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealMatchingCardsFromTargetHandAndKeepEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new TrackChosenCardExiledWithSourceEffect(), false));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SeekLibraryForOwnerOfCardExiledWithSourceBelowManaValueEffect());
    }
}
