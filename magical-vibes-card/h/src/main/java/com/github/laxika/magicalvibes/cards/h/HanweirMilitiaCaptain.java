package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WestvaleCultLeader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "21")
public class HanweirMilitiaCaptain extends Card {

    public HanweirMilitiaCaptain() {
        setBackFaceCard(new WestvaleCultLeader());

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanentCount(4, new PermanentIsCreaturePredicate())),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "WestvaleCultLeader";
    }
}
