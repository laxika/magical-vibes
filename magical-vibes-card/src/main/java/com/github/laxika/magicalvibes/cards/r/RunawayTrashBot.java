package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "257")
public class RunawayTrashBot extends Card {

    public RunawayTrashBot() {
        CardsInGraveyard artifactOrEnchantmentCards = new CardsInGraveyard(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.ENCHANTMENT))),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(artifactOrEnchantmentCards, new Fixed(0)));
    }
}
