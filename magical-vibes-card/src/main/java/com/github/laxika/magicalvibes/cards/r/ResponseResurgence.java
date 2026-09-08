package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "229")
public class ResponseResurgence extends Card {

    public ResponseResurgence() {
        TargetFilter attackingOrBlockingCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentIsBlockingPredicate())))),
                "Target must be an attacking or blocking creature.");

        CardEffect response = new DealDamageToTargetCreatureEffect(5);
        CardEffect firstStrikeAndVigilance = new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE), GrantScope.OWN_CREATURES);
        CardEffect additionalCombatAndMainPhase = new AdditionalCombatMainPhaseEffect(1);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Response — Response deals 5 damage to target attacking or blocking creature",
                        response,
                        attackingOrBlockingCreature)
                        .withManaCost("{R/W}{R/W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Resurgence — Creatures you control gain first strike and vigilance until end of turn. After this main phase, there is an additional combat phase followed by an additional main phase",
                        List.of(
                                firstStrikeAndVigilance, additionalCombatAndMainPhase))
                        .withManaCost("{3}{R}{W}")
        )));
    }
}
