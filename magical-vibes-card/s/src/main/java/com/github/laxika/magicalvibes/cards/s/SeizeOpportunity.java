package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "119")
public class SeizeOpportunity extends Card {

    public SeizeOpportunity() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. Until the end of your next turn, you may play those cards",
                        new ExileTopCardMayPlayThisTurnEffect(2, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)),
                new ChooseOneEffect.ChooseOneOption(
                        "Up to two target creatures each get +2/+1 until end of turn",
                        List.<CardEffect>of(BoostEffect.target(2, 1)),
                        TargetFilters.creature(), null, 0, 2, false, null)
        )));
    }
}
