package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect;
import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "42")
public class KnacksawClique extends Card {

    public KnacksawClique() {
        // Flying (from Scryfall)
        // {1}{U}, {Q}: Target opponent exiles the top card of their library. Until end of turn, you may play that card.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(new ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect()),
                "{1}{U}, {Q}: Target opponent exiles the top card of their library. "
                        + "Until end of turn, you may play that card."
        ).withRequiresUntap());
    }
}
