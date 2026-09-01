package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "268")
public class FireNationPalace extends Card {

    public FireNationPalace() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0, new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC)
                ))),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new GrantKeywordEffect(Keyword.FIREBENDING, GrantScope.TARGET),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_ATTACK,
                                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 4))
                ),
                "{1}{R}, {T}: Target creature you control gains firebending 4 until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
