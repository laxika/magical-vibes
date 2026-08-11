package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "211")
public class KethisTheHiddenHand extends Card {

    public KethisTheHiddenHand() {
        CardSupertypePredicate legendary = new CardSupertypePredicate(CardSupertype.LEGENDARY);

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                legendary, 1, CostModificationScope.SELF));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null, legendary),
                        new AllowPlayMatchingCardsFromGraveyardThisTurnEffect(legendary)),
                "Exile two legendary cards from your graveyard: Until end of turn, each legendary card in your graveyard gains \"You may play this card from your graveyard.\""
        ));
    }
}
