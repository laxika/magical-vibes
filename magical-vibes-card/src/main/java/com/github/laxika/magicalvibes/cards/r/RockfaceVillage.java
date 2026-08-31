package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "259")
public class RockfaceVillage extends Card {

    private static final PermanentPredicate KINDRED_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentControlledBySourceControllerPredicate(),
            new PermanentHasAnySubtypePredicate(Set.of(
                    CardSubtype.LIZARD,
                    CardSubtype.MOUSE,
                    CardSubtype.OTTER,
                    CardSubtype.RACCOON))
    ));

    public RockfaceVillage() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.RED, 1, new ManaRestriction.CreatureSpells())),
                "{T}: Add {R}. Spend this mana only to cast a creature spell."));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new BoostTargetCreatureEffect(1, 0, KINDRED_CREATURE),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{R}, {T}: Target Lizard, Mouse, Otter, or Raccoon you control gets +1/+0 and gains haste until end of turn. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        KINDRED_CREATURE,
                        "Target must be a Lizard, Mouse, Otter, or Raccoon you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
