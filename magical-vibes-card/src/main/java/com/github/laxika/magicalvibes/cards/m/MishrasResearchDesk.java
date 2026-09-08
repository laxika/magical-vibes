package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;
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
                        new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(2)),
                "{1}, {T}, Sacrifice this artifact: Exile the top two cards of your library. "
                        + "Choose one of them. Until the end of your next turn, you may play that card."
        ));

        addUnearth("{1}{R}");
    }
}
