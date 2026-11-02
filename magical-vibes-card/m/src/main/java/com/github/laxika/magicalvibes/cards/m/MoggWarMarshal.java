package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "170")
@CardRegistration(set = "DD1", collectorNumber = "45")
@CardRegistration(set = "EVG", collectorNumber = "45")
@CardRegistration(set = "MMA", collectorNumber = "122")
@CardRegistration(set = "EMA", collectorNumber = "139")
@CardRegistration(set = "SLD", collectorNumber = "1618")
@CardRegistration(set = "TSR", collectorNumber = "176")
@CardRegistration(set = "DMR", collectorNumber = "131")
public class MoggWarMarshal extends Card {

    public MoggWarMarshal() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{1}{R}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS), goblinToken());
        addTrigger(Triggers.dies(ObjectRelation.THIS), goblinToken());
    }

    private static CreateTokenEffect goblinToken() {
        return new CreateTokenEffect("Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN), Set.of(), Set.of());
    }
}
