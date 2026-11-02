package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplaceInstantOrSorceryDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "277")
public class ForethoughtAmulet extends Card {

    public ForethoughtAmulet() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ForcedCostOrElseEffect(
                new PayManaCost("{3}"),
                List.of(new SacrificeSelfEffect()),
                true));
        addStaticEffect(new ReplaceInstantOrSorceryDamageToControllerEffect(3, 2));
    }
}
