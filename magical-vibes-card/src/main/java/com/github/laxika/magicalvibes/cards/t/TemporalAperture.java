package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardGrantFreePlayUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "310")
public class TemporalAperture extends Card {

    public TemporalAperture() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ShuffleLibraryEffect(false),
                        new RevealTopCardGrantFreePlayUntilEndOfTurnEffect()
                ),
                "{5}, {T}: Shuffle your library, then reveal the top card. Until end of turn, for as long as that card remains on top of your library, play with the top card of your library revealed and you may play that card without paying its mana cost."
        ));
    }
}
