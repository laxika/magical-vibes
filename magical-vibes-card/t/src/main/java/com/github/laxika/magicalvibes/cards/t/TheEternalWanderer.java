package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CombatAttackTargetScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "11")
public class TheEternalWanderer extends Card {

    public TheEternalWanderer() {
        addEffect(EffectSlot.STATIC,
                new MaximumCombatCreaturesEffect(1, Integer.MAX_VALUE, CombatAttackTargetScope.SOURCE_PERMANENT));

        var artifactOrCreature = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())),
                "Target must be an artifact or creature");
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(FlickerEffect.exileTargetReturnAtOwnerNextEndStep()),
                "+1: Exile up to one target artifact or creature. Return that card to the battlefield under its owner's control at the beginning of that player's next end step.",
                artifactOrCreature,
                1,
                null,
                null,
                List.of(),
                0,
                1));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        1,
                        "Samurai",
                        2,
                        2,
                        CardColor.WHITE,
                        List.of(CardSubtype.SAMURAI),
                        Set.of(Keyword.DOUBLE_STRIKE),
                        Set.of())),
                "0: Create a 2/2 white Samurai creature token with double strike."));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new EachPlayerSacrificesDownToCountEffect(
                        1, new PermanentIsCreaturePredicate())),
                "−4: For each player, choose a creature that player controls. Each player sacrifices all creatures they control not chosen this way."));
    }
}
