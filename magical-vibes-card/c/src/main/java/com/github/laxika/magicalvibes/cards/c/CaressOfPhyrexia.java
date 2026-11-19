package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "NPH", collectorNumber = "53")
@CardRegistration(set = "ONC", collectorNumber = "92")
public class CaressOfPhyrexia extends Card {

    public CaressOfPhyrexia() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addSpellEffect(new DrawCardEffect(3, Subjects.targetPlayerDeclaredElsewhere()))
                .addSpellEffect(new LoseLifeEffect(3, Subjects.targetPlayer()))
                .addSpellEffect(new GivePoisonCountersEffect(3, PoisonRecipient.TARGET_PLAYER));
    }
}
