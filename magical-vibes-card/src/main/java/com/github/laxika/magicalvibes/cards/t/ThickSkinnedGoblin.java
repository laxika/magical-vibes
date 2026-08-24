package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeEchoCostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "182")
public class ThickSkinnedGoblin extends Card {

    public ThickSkinnedGoblin() {
        addEffect(EffectSlot.STATIC, new AlternativeEchoCostEffect("{0}"));
        addActivatedAbility(new ActivatedAbility(
            false,
            "{R}",
            List.of(new GrantProtectionFromColorUntilEndOfTurnEffect(CardColor.RED, GrantScope.SELF)),
            "{R}: This creature gains protection from red until end of turn."
        ));
    }
}
