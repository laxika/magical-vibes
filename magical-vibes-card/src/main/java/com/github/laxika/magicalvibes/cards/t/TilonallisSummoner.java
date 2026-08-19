package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtEndStepUnlessConditionEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCreateXTokensEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "121")
public class TilonallisSummoner extends Card {

    public TilonallisSummoner() {
        addEffect(EffectSlot.STATIC, new AscendEffect());

        CreateTokenEffect elementalToken = new CreateTokenEffect(
                1, "Elemental", 1, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL), true);
        addEffect(EffectSlot.ON_ATTACK, new PayXManaCreateXTokensEffect("{X}{R}", elementalToken));
        addEffect(EffectSlot.ON_ATTACK,
                new ExileCreatedPermanentsAtEndStepUnlessConditionEffect(new ControllerHasCityBlessing()));
    }
}
