package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "367")
@CardRegistration(set = "ICE", collectorNumber = "317")
@CardRegistration(set = "DKM", collectorNumber = "35")
@CardRegistration(set = "ME2", collectorNumber = "207")
public class ElkinBottle extends Card {

    public ElkinBottle() {
        // {3}, {T}: Exile the top card of your library. Until the beginning of your next upkeep, you
        // may play that card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(false).until(Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP)),
                "{3}, {T}: Exile the top card of your library. Until the beginning of your next "
                        + "upkeep, you may play that card."));
    }
}
