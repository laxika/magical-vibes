package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "34")
public class TheKenrithsRoyalFuneral extends Card {

    public TheKenrithsRoyalFuneral() {
        var legendaryCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSupertypePredicate(CardSupertype.LEGENDARY)));
        var greatestExiledManaValue = new GreatestManaValueAmongCardsExiledWithSource();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileCardsFromGraveyardEffect(2, legendaryCreature, true, true, true));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(greatestExiledManaValue));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LoseLifeEffect(greatestExiledManaValue, LoseLifeRecipient.CONTROLLER));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardsExiledWithSource(), CostModificationScope.SELF));
    }
}
