package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEnduringStory;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "14")
public class FLiThePathfinder extends Card {

    public FLiThePathfinder() {
        addEffect(EffectSlot.STATIC, new StoriedEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHasEnduringStory(),
                new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES)));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.DWARF),
                                new CardNotPredicate(new CardIsTokenPredicate())
                        )),
                        new CreateTokenEffect("Dwarf", 2, 2, CardColor.RED,
                                List.of(CardSubtype.DWARF), Set.of(), Set.of())
                ));
    }
}
