package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "98")
public class BilboUnexpectedAdventurer extends Card {

    public BilboUnexpectedAdventurer() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtLeastPredicate(3)));

        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                new CardMaxManaValuePredicate(3))))
                        .targetGraveyard(true)
                        .upTo(true)
                        .underOwnersControl(true)
                        .build());
    }
}
