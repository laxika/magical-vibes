package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "223")
public class CollisionColossus extends Card {

    public CollisionColossus() {
        PermanentAllOfPredicate flyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
        TargetFilter flyingCreatureTarget = new PermanentPredicateTargetFilter(
                flyingCreature, "Target must be a creature with flying.");
        TargetFilter creatureTarget = TargetFilters.creature();

        CardEffect collision = new DealDamageToTargetCreatureEffect(6, flyingCreature);
        List<CardEffect> colossus = List.of(
                new BoostTargetCreatureEffect(4, 2),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Collision — Collision deals 6 damage to target creature with flying",
                        collision,
                        flyingCreatureTarget
                ).withManaCost("{1}{R/G}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Colossus — Target creature gets +4/+2 and gains trample until end of turn",
                        colossus,
                        creatureTarget
                ).withManaCost("{R}{G}")
        )));
    }
}
