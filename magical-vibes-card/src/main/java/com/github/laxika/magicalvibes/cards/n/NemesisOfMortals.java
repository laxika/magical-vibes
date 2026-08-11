package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "163")
public class NemesisOfMortals extends Card {

    public NemesisOfMortals() {
        CardsInGraveyard creatureCardsInGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);

        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(creatureCardsInGraveyard));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}{G}{G}",
                List.of(new ReduceActivationCostEffect(creatureCardsInGraveyard), new MonstrosityEffect(5)),
                "{7}{G}{G}: Monstrosity 5. This ability costs {1} less to activate for each creature card in your graveyard."
        ).withActivationCondition(new NotCondition(new SourceIsMonstrous()), "This creature is already monstrous"));
    }
}
