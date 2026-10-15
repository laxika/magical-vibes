package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "193")
public class TheUnbeatableSquirrelGirl extends Card {

    public TheUnbeatableSquirrelGirl() {
        CreateTokenEffect squirrel = new CreateTokenEffect(
                1, "Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, squirrel);
        addEffect(EffectSlot.ON_ATTACK, squirrel);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{G}{G}",
                List.of(new CreateTokenEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL), CountScope.CONTROLLER),
                        "Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())),
                "{1}{G}{G}{G}: Create X 1/1 green Squirrel creature tokens, where X is the number of Squirrels you control."
        ));
    }
}
