package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsSourceIntensityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "20")
public class JarsylDarkAgeScion extends Card {

    public JarsylDarkAgeScion() {
        setStartingIntensity(1);

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new CastCardFromGraveyardEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new CardManaValueEqualsSourceIntensityPredicate())),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                new CardAnyOfPredicate(List.of()),
                false,
                true,
                new IntensifySourceCardEffect(1)));
    }
}
