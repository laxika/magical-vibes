package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "93")
@CardRegistration(set = "FDN", collectorNumber = "332")
@CardRegistration(set = "FDN", collectorNumber = "394")
@CardRegistration(set = "FDN", collectorNumber = "468")
public class SearslicerGoblin extends Card {

    public SearslicerGoblin() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Raid()),
                new CreateTokenEffect("Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
