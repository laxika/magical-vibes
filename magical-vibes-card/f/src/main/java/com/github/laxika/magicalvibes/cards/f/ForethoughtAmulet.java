package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplaceInstantOrSorceryDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "277")
public class ForethoughtAmulet extends Card {

    public ForethoughtAmulet() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new PayManaCost("{3}"),
                List.of(new SacrificeSelfEffect()),
                true));
        addEffect(EffectSlot.STATIC, new ReplaceInstantOrSorceryDamageToControllerEffect(3, 2));
    }
}
