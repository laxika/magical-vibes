package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1529")
@CardRegistration(set = "CMM", collectorNumber = "254")
@CardRegistration(set = "AFC", collectorNumber = "140")
@CardRegistration(set = "FDC", collectorNumber = "176")
public class SkylineDespot extends Card {

    public SkylineDespot() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerIsMonarch()),
                new CreateTokenEffect("Dragon", 5, 5, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of()));
    }
}
