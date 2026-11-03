package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsMoreLandsThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "258")
public class GreenerPastures extends Card {

    public GreenerPastures() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new ActivePlayerControlsMoreLandsThanEachOtherPlayer()),
                new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(
                        "Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of())));
    }
}
