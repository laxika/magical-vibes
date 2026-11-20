package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "182")
public class DisturbedSlumber extends Card {

    public DisturbedSlumber() {
        target(TargetFilters.landYouControl())
                .addSpellEffect(new AnimatePermanentsEffect(
                        4, 4,
                        List.of(CardSubtype.DINOSAUR), Set.of(Keyword.REACH, Keyword.HASTE),
                        null, Set.of(), Subjects.target(), Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(SetCombatRequirementThisTurnEffect.forAnimatedPermanent(
                                CombatRequirement.MUST_BE_BLOCKED));
    }
}
