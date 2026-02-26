package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardArtifactOnlyColorlessManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "33")
public class GrandArchitect extends Card {

    public GrandArchitect() {
        // Ability 1: Other blue creatures you control get +1/+1
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(),
                GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));

        // Ability 2: {U}: Target artifact creature becomes blue until end of turn
        addActivatedAbility(new ActivatedAbility(
                false, "{U}",
                List.of(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE)),
                "{U}: Target artifact creature becomes blue until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate())),
                        "Target must be an artifact creature")
        ));

        // Ability 3: Tap an untapped blue creature you control: Add {C}{C}
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapCreatureCost(new PermanentColorInPredicate(Set.of(CardColor.BLUE))),
                        new AwardArtifactOnlyColorlessManaEffect(2)),
                "Tap an untapped blue creature you control: Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts."
        ));
    }
}
