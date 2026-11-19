package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.PermanentsPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "28")
@CardRegistration(set = "SOC", collectorNumber = "77")
public class OminousHarvest extends Card {

    public OminousHarvest() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addSpellEffect(new DrawCardEffect(1, Subjects.targetPlayer()))
                .addSpellEffect(new LoseLifeEffect(1, Subjects.targetPlayer()))
                .addTrigger(Triggers.youCastThisSpell(),
                        new CopyTriggeringSpellEffect().withCopies(new PermanentsPutIntoGraveyardFromBattlefieldThisTurn()));
    }
}
