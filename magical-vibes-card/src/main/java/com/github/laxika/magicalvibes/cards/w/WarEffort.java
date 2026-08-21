package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "131")
public class WarEffort extends Card {

    public WarEffort() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new CreateTokenEffect(
                1, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
