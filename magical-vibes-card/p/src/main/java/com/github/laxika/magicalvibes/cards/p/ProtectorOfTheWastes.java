package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "14")
@CardRegistration(set = "TDC", collectorNumber = "54")
public class ProtectorOfTheWastes extends Card {

    public ProtectorOfTheWastes() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new MonstrosityEffect(3)),
                "{4}{W}: Monstrosity 3."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        PermanentAnyOfPredicate artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));
        target(new PermanentPredicateTargetFilter(artifactOrEnchantment,
                "Target must be an artifact or enchantment"), 0, 2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentEffect());
        addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, new ExileTargetPermanentEffect());
    }
}
