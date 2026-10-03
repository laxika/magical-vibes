package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextTurnDeclareAttackersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "460")
public class NoctisHeirApparent extends Card {

    public NoctisHeirApparent() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                "Target must be an Equipment you control"));
        target(TargetFilters.creatureYouControl());
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new ConditionalEffect(new DuringCombat(),
                        new MayEffect(new AttachTargetEquipmentToTargetCreatureEffect(),
                                "Attach target Equipment you control to target creature you control?")));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileSelfReturnAtNextTurnDeclareAttackersEffect(true)),
                "{3}: Exile Noctis. Return it to the battlefield under its owner's control tapped and attacking at the beginning of that player's next declare attackers step. It can't be blocked that combat."));
    }
}
