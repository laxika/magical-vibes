package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "135")
public class RaisedByWolves extends Card {

    public RaisedByWolves() {
        PermanentCount wolves = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.WOLF), CountScope.CONTROLLER);

        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                        2, "Wolf", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.WOLF), Set.of(), Set.of()))
                .addStaticEffect(new AttachedBoostEffect(
                        wolves, wolves, GrantScope.ENCHANTED_CREATURE));
    }
}
