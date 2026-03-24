package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "245")
public class SentinelTotem extends Card {

    public SentinelTotem() {
        // When Sentinel Totem enters the battlefield, scry 1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        // {T}, Exile Sentinel Totem: Exile all cards from all graveyards.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileSelfCost(), new ExileAllGraveyardsEffect()),
                "{T}, Exile Sentinel Totem: Exile all cards from all graveyards."
        ));
    }
}
