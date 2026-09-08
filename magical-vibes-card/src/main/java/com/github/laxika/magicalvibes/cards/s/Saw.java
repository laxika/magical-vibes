package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "254")
public class Saw extends Card {

    private static final PermanentAllOfPredicate OTHER_PERMANENT = new PermanentAllOfPredicate(List.of(
            new PermanentTruePredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
            new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate()),
            new PermanentNotPredicate(new PermanentIsTriggeringPermanentPredicate())));

    public Saw() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(OTHER_PERMANENT, new DrawCardEffect(1), "a permanent"),
                "Sacrifice a permanent other than that creature or this Equipment?"));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
