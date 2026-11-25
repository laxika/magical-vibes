package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import static com.github.laxika.magicalvibes.model.effect.Subjects.targetPlayer;

@CardRegistration(set = "NCC", collectorNumber = "8")
@CardRegistration(set = "NCC", collectorNumber = "106")
public class ParnesseTheSubtleBrush extends Card {

    public ParnesseTheSubtleBrush() {
        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPaysLife(4));

        var targetOpponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        target(targetOpponent, 0, 1).addTrigger(Triggers.youCopySpell().anySpell(),
                new CopyTriggeringSpellEffect().mayBeCopiedBy(targetPlayer()));
    }
}
