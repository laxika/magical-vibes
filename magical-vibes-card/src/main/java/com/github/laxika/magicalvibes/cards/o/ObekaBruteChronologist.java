package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1579")
@CardRegistration(set = "SLZ", collectorNumber = "92")
@CardRegistration(set = "SLZ", collectorNumber = "213")
@CardRegistration(set = "SLZ", collectorNumber = "334")
public class ObekaBruteChronologist extends Card {

    public ObekaBruteChronologist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(
                        new EndTurnEffect(),
                        "End the turn?",
                        null,
                        MayChoicePlayer.ACTIVE_PLAYER)),
                "{T}: The player whose turn it is may end the turn."
        ));
    }
}
