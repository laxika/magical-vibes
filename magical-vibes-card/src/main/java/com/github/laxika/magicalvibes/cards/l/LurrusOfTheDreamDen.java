package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "226")
public class LurrusOfTheDreamDen extends Card {

    public LurrusOfTheDreamDen() {
        addEffect(EffectSlot.STATIC, new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new CardMaxManaValuePredicate(2)))));
    }
}
