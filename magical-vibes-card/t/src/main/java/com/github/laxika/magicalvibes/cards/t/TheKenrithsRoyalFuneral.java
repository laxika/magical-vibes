package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "34")
@CardRegistration(set = "MAT", collectorNumber = "84")
@CardRegistration(set = "MAT", collectorNumber = "134")
@CardRegistration(set = "MAT", collectorNumber = "169")
@CardRegistration(set = "MAT", collectorNumber = "213")
public class TheKenrithsRoyalFuneral extends Card {

    public TheKenrithsRoyalFuneral() {
        var legendaryCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSupertypePredicate(CardSupertype.LEGENDARY)));
        var greatestExiledManaValue = new GreatestManaValueAmongCardsExiledWithSource();

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileCardsFromGraveyardEffect(2, legendaryCreature, true, true, true),
                new DrawCardEffect(greatestExiledManaValue),
                new LoseLifeEffect(greatestExiledManaValue, LoseLifeRecipient.CONTROLLER));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardsExiledWithSource(), CostModificationScope.SELF));
    }
}
