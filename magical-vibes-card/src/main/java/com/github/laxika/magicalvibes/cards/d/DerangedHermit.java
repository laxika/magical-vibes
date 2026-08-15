package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ULG", collectorNumber = "101")
public class DerangedHermit extends Card {

    public DerangedHermit() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                4, "Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{3}{G}{G}"));
    }
}
