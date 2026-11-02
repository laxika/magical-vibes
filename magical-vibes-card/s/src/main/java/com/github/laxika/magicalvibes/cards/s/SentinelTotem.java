package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "245")
public class SentinelTotem extends Card {

    public SentinelTotem() {
        // When Sentinel Totem enters the battlefield, scry 1.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(1));

        // {T}, Exile Sentinel Totem: Exile all cards from all graveyards.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS)),
                "{T}, Exile Sentinel Totem: Exile all cards from all graveyards."
        ));
    }
}
