package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "162")
public class MishrasResearchDesk extends Card {

    public MishrasResearchDesk() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new ExileTopCardMayPlayThisTurnEffect(2, false)
                                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN).choosingOne()),
                "{1}, {T}, Sacrifice this artifact: Exile the top two cards of your library. "
                        + "Choose one of them. Until the end of your next turn, you may play that card."
        ));

        addUnearth("{1}{R}");
    }
}
