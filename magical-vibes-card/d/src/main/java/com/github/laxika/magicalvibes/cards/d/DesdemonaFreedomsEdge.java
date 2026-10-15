package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "101")
@CardRegistration(set = "PIP", collectorNumber = "412")
@CardRegistration(set = "PIP", collectorNumber = "629")
@CardRegistration(set = "PIP", collectorNumber = "940")
public class DesdemonaFreedomsEdge extends Card {

    public DesdemonaFreedomsEdge() {
        CardAnyOfPredicate artifactOrLowManaValue = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardMaxManaValuePredicate(3)));
        addEffect(EffectSlot.ON_ATTACK, GrantTargetGraveyardCardCastEffect.withEscape(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        artifactOrLowManaValue)),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                2));
    }
}
