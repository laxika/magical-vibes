package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "162")
public class TheThirdDoctor extends Card {

    public TheThirdDoctor() {
        PermanentCount noncreatureTokens = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                CountScope.CONTROLLER);

        addEffect(EffectSlot.STATIC,
                new DynamicStaticBoostEffect(noncreatureTokens, noncreatureTokens, GrantScope.SELF));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Clue token", CreateTokenEffect.ofClueToken(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Food token", CreateTokenEffect.ofFoodToken(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Treasure token", CreateTokenEffect.ofTreasureToken(1))
        )));
    }
}
