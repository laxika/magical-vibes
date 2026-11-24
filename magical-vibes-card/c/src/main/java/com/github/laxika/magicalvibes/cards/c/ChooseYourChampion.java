package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantCastSpellsAndAttackWithCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "DSC", collectorNumber = "330")
public class ChooseYourChampion extends Card {

    public ChooseYourChampion() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        )).addSpellEffect(new ChoosePlayerEffect(Subjects.eachPlayer(), "a player")
                .chosenBy(Subjects.targetPlayer()));
        // "Only you and the chosen player can cast spells and attack with creatures": every other player can't.
        addSpellEffect(new GrantStaticEffectToPlayerEffect(new PlayerCantCastSpellsAndAttackWithCreaturesEffect(),
                Subjects.except(Subjects.eachPlayer(),
                        Subjects.allOf(Subjects.you(), Subjects.playersThisWay(StepKind.CHOOSE))),
                Duration.UNTIL_YOUR_NEXT_TURN));
    }
}
