package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OilCounterEventThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "127")
public class ChurningReservoir extends Card {

    public ChurningReservoir() {
        var anotherNontokenArtifactOrCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.OIL, 1, anotherNontokenArtifactOrCreatureYouControl));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new CreateTokenEffect(
                        "Phyrexian Goblin", 1, 1, CardColor.RED,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN), Set.of(), Set.of())),
                "{2}, {T}: Create a 1/1 red Phyrexian Goblin creature token. Activate only if an oil counter was removed from a permanent you controlled this turn or a permanent with an oil counter on it was put into a graveyard this turn."
        ).withActivationCondition(
                new OilCounterEventThisTurn(),
                "Activate only if an oil counter was removed from a permanent you controlled this turn or a permanent with an oil counter on it was put into a graveyard this turn"));
    }
}
