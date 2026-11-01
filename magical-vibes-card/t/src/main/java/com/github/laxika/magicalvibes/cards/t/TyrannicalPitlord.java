package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "284")
public class TyrannicalPitlord extends Card {

    public TyrannicalPitlord() {
        addStaticEffect(new ChooseAnotherCreatureOnEnterEffect());
        addStaticEffect(new StaticBoostEffect(
                3, 3, Set.of(Keyword.FLYING), GrantScope.ALL_CREATURES,
                new PermanentIsChosenPermanentPredicate()));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE));
    }
}
