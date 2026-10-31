package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;

@CardRegistration(set = "DMU", collectorNumber = "151")
public class WarhostsFrenzy extends Card {

    public WarhostsFrenzy() {
        addStaticEffect(new KickerEffect("{B}"));
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 0));
        addSpellEffect(new ConditionalEffect(new Kicked(),
                new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        EffectSlot.ON_ALLY_CREATURE_DIES,
                        new DrawCardEffect(1))));
    }
}
