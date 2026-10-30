package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "6")
public class ExpeditionSupplier extends Card {

    public ExpeditionSupplier() {
        // "Whenever one or more Humans and/or Warriors you control enter" includes Expedition Supplier
        // itself. The batch slot only watches other creatures (and tokens), so the self-or-ally slot covers
        // the Supplier's own entry; the once-each-turn limit keeps the two from both producing a Knife.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD, conjureKnifeForHumanOrWarrior());
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD, conjureKnifeForHumanOrWarrior());
    }

    private static TriggeringCardConditionalEffect conjureKnifeForHumanOrWarrior() {
        return new TriggeringCardConditionalEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.HUMAN),
                        new CardSubtypePredicate(CardSubtype.WARRIOR))),
                new OncePerTurnTriggerEffect(new ConjureCardToBattlefieldEffect("ZNR", "256")));
    }
}
