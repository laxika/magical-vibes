package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsAndSeparateEffect(4));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SPHINX),
                        new RevealTopCardsAndSeparateEffect(4)));
    }
}
