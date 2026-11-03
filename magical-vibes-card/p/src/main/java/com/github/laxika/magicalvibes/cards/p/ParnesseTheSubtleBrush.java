package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CopyCopiedSpellForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "8")
@CardRegistration(set = "NCC", collectorNumber = "106")
public class ParnesseTheSubtleBrush extends Card {

    public ParnesseTheSubtleBrush() {
        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysLifeEffect(new Fixed(4)));

        var targetOpponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
        target(targetOpponent, 0, 1).addTrigger(Triggers.youCopySpell().anySpell(),
                new MayEffect(new CopyCopiedSpellForTargetPlayerEffect(),
                        "Copy that spell?", null, MayChoicePlayer.TARGET_PLAYER));
    }
}
