package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "130")
public class ElementalUprising extends Card {

    public ElementalUprising() {
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                        4, 4,
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.HASTE),
                        null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.SPELL,
                        SetCombatRequirementThisTurnEffect.forAnimatedPermanent(
                                CombatRequirement.MUST_BE_BLOCKED));
    }
}
