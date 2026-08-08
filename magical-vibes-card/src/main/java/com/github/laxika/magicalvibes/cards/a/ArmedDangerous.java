package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "122")
public class ArmedDangerous extends Card {

    public ArmedDangerous() {
        setAllowSharedTargets(true);

        TargetFilter creature = TargetFilters.creature();
        CardEffect armed = SequenceEffect.of(
                new BoostTargetCreatureEffect(1, 1),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
        CardEffect dangerous = new SetCombatRequirementThisTurnEffect(
                CombatRequirement.MUST_BE_BLOCKED_BY_ALL);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Armed — Target creature gets +1/+1 and gains double strike until end of turn",
                        armed,
                        creature
                ).withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Dangerous — All creatures able to block target creature this turn do so",
                        dangerous,
                        creature
                ).withManaCost("{3}{G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Armed and then Dangerous",
                        List.of(armed, dangerous),
                        List.of(creature, creature)
                ).withManaCost("{4}{G}{R}")
        )));
    }
}
