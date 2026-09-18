package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;

import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "284")
public class TyrannicalPitlord extends Card {

    public TyrannicalPitlord() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseAnotherCreatureOnEnterEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                3, 3, Set.of(Keyword.FLYING), GrantScope.ALL_CREATURES,
                new PermanentIsChosenPermanentPredicate()));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE));
    }
}
