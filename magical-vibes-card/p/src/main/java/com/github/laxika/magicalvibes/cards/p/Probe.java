package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "INV", collectorNumber = "66")
public class Probe extends Card {

    public Probe() {
        addStaticEffect(new KickerEffect("{1}{B}"));
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        targetWhenKicked(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 0, 1, 1).addSpellEffect(new ConditionalEffect(new Kicked(), new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)));
    }
}
