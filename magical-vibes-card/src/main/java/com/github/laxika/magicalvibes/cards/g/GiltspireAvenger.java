package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageToSourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "108")
public class GiltspireAvenger extends Card {

    public GiltspireAvenger() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until end
        // of turn. ON_ALLY_CREATURE_ATTACKS fires per attacking ally and records the attacker as the
        // trigger's (non-targeting) target; AttacksAlone restricts the boost to lone attackers.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // {T}: Destroy target creature that dealt damage to you this turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DestroyTargetPermanentEffect()),
                "{T}: Destroy target creature that dealt damage to you this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentDealtDamageToSourceControllerThisTurnPredicate()
                        )),
                        "Target must be a creature that dealt damage to you this turn"
                )));
    }
}
