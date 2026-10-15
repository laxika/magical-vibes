package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNoAbilitiesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "33")
@CardRegistration(set = "DMC", collectorNumber = "55")
public class JasmineBorealOfTheSeven extends Card {

    public JasmineBorealOfTheSeven() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(
                                ManaColor.GREEN, 1, new ManaRestriction.CreatureSpellsWithoutAbilities()),
                        new AwardRestrictedManaEffect(
                                ManaColor.WHITE, 1, new ManaRestriction.CreatureSpellsWithoutAbilities())
                ),
                "{T}: Add {G}{W}. Spend this mana only to cast creature spells with no abilities."
        ));

        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasNoAbilitiesPredicate())
                )),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasNoAbilitiesPredicate()
                )),
                "Creatures with abilities can't block creatures you control with no abilities"
        ));
    }
}
