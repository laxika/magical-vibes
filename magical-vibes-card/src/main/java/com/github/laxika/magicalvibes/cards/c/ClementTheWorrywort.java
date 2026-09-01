package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueLessThanSourceManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "209")
public class ClementTheWorrywort extends Card {

    public ClementTheWorrywort() {
        PermanentPredicate bounceFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentManaValueLessThanSourceManaValuePredicate()));

        target(new PermanentPredicateTargetFilter(
                bounceFilter,
                "Target must be a creature you control with lesser mana value"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnToHandEffect.target())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, ReturnToHandEffect.target());

        PermanentPredicate frogFilter = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FROG));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardRestrictedManaEffect(
                                ManaColor.GREEN, 1, new ManaRestriction.CreatureSpells())),
                        "{T}: Add {G}. Spend this mana only to cast a creature spell."),
                GrantScope.ALL_OWN_CREATURES,
                frogFilter));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardRestrictedManaEffect(
                                ManaColor.BLUE, 1, new ManaRestriction.CreatureSpells())),
                        "{T}: Add {U}. Spend this mana only to cast a creature spell."),
                GrantScope.ALL_OWN_CREATURES,
                frogFilter));
    }
}
