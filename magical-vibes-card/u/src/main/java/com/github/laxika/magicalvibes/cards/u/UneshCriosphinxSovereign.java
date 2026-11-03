package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "52")
@CardRegistration(set = "AKR", collectorNumber = "85")
public class UneshCriosphinxSovereign extends Card {

    public UneshCriosphinxSovereign() {
        // Sphinx spells you cast cost {2} less to cast.
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.SPHINX), 2, CostModificationScope.SELF));

        // Whenever Unesh or another Sphinx you control enters, reveal the top four cards of your
        // library. An opponent separates those cards into two piles. Put one pile into your hand
        // and the other into your graveyard. Two slots: Unesh's own ETB, plus other Sphinxes.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RevealTopCardsAndSeparateEffect(4));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.SPHINX))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new RevealTopCardsAndSeparateEffect(4));
    }
}
