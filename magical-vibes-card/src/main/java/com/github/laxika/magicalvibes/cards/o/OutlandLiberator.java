package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FrenziedTrapbreaker;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "190")
public class OutlandLiberator extends Card {

    public OutlandLiberator() {
        setBackFaceCard(new FrenziedTrapbreaker());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{1}, Sacrifice this creature: Destroy target artifact or enchantment.",
                artifactOrEnchantmentFilter()));

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    private static PermanentPredicateTargetFilter artifactOrEnchantmentFilter() {
        PermanentPredicate artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));
        return new PermanentPredicateTargetFilter(
                artifactOrEnchantment,
                "Target must be an artifact or enchantment");
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "FrenziedTrapbreaker";
    }
}
