package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "18")
public class MarduWoeReaper extends Card {

    public MarduWoeReaper() {
        // Whenever this creature or another Warrior you control enters, you may exile target
        // creature card from a graveyard. If you do, you gain 1 life.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new MayEffect(
                        new ExileGraveyardCardWithConditionalBonusEffect(
                                1, 0, 0, 0, 0, 0,
                                GraveyardSearchScope.ALL_GRAVEYARDS,
                                new CardTypePredicate(CardType.CREATURE)),
                        "Exile target creature card from a graveyard?"));
    }
}
