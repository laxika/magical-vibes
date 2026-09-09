package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "147")
public class HobgoblinBanditLord extends Card {

    public HobgoblinBanditLord() {
        var goblinPermanent = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, goblinPermanent));

        var goblinsEntered = new PermanentsEnteredBattlefieldThisTurn(
                new CardSubtypePredicate(CardSubtype.GOBLIN), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new DealDamageToAnyTargetEffect(goblinsEntered)),
                "{R}, {T}: This creature deals damage equal to the number of Goblins that entered the battlefield under your control this turn to any target."));
    }
}
