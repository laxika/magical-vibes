package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "99")
public class DungeonCrawler extends Card {

    public DungeonCrawler() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(new TriggerCondition(Triggers.youCompleteDungeon(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Dungeon Crawler to your hand?"));
    }
}
