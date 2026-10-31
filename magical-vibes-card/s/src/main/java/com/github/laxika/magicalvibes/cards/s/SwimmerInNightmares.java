package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "275")
public class SwimmerInNightmares extends Card {

    public SwimmerInNightmares() {
        addStaticEffect(new ConditionalEffect(
                new AnyGraveyardAtLeast(10),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));

        addStaticEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ASHIOK)))),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
    }
}
