package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "58")
public class AerialCaravan extends Card {

    public AerialCaravan() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{U}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(false)),
                "{1}{U}{U}: Exile the top card of your library. Until end of turn, you may play that card."
        ));
    }
}
