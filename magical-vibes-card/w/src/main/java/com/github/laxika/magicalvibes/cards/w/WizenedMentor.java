package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DRC", collectorNumber = "8")
@CardRegistration(set = "DRC", collectorNumber = "24")
public class WizenedMentor extends Card {

    public WizenedMentor() {
        addTrigger(Triggers.opponentActivatesNonManaAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CreateTokenEffect("Zombie", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));
    }
}
