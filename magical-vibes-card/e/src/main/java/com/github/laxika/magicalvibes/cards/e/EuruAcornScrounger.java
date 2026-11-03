package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "24")
public class EuruAcornScrounger extends Card {

    public EuruAcornScrounger() {
        // "When you do" is a reflexive trigger (CR 603.12), so the conjure goes on the stack after forage.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new ForageEffect(new QueueReflexiveAbilityEffect(
                        new ConjureCardToBattlefieldEffect("MH2", "153"))),
                "Forage?"));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL)),
                TriggerOptions.ONE_OR_MORE,
                new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentIsTokenPredicate(),
                                new PutCounterOnEachControlledPermanentEffect(
                                        CounterType.ACORN, 1,
                                        new PermanentNamedPredicate("Chitterspitter")),
                                "a token"),
                        "Sacrifice a token?"));
    }
}
