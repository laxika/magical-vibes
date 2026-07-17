package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayUntilNextUpkeepEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "367")
public class ElkinBottle extends Card {

    public ElkinBottle() {
        // {3}, {T}: Exile the top card of your library. Until the beginning of your next upkeep, you
        // may play that card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new ExileTopCardMayPlayUntilNextUpkeepEffect()),
                "{3}, {T}: Exile the top card of your library. Until the beginning of your next "
                        + "upkeep, you may play that card."));
    }
}
