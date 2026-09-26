package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "77")
@CardRegistration(set = "LTC", collectorNumber = "157")
public class LothlorienBlade extends Card {

    private static final PermanentHasSubtypePredicate ELF =
            new PermanentHasSubtypePredicate(CardSubtype.ELF);

    public LothlorienBlade() {
        setAttachRestriction(ELF);
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature defending player controls"))
                .addEffect(EffectSlot.ON_ATTACK,
                        new EquippedCreatureDealsPowerDamageToTargetCreatureEffect());

        addActivatedAbility(new EquipActivatedAbility(
                "{2}", ELF, "Target must be an Elf creature you control"));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
