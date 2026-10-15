package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOwnCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/** Hustle // Bustle, a split spell with one mode for each half. */
@CardRegistration(set = "MKM", collectorNumber = "249")
public class HustleBustle extends Card {

    public HustleBustle() {
        List<CardEffect> bustle = List.of(
                new BoostAllOwnCreaturesEffect(2, 2),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES),
                new MayEffect(new TurnOwnCreatureFaceUpEffect(), "Turn a creature you control face up?"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Hustle - Target creature attacks or blocks this turn if able",
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK_OR_BLOCK),
                        TargetFilters.creature()
                ).withManaCost("{U/R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Bustle - Creatures you control get +2/+2 and gain trample until end of turn. You may turn a creature you control face up",
                        bustle
                ).withManaCost("{4}{R/G}{R/G}")
        )));
    }
}
