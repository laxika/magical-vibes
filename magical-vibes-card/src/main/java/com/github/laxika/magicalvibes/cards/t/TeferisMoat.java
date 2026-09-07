package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "279")
@CardRegistration(set = "TSB", collectorNumber = "103")
public class TeferisMoat extends Card {

    public TeferisMoat() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new PermanentNotPredicate(new PermanentHasSourceChosenColorPredicate())))));
    }
}
