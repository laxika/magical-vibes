package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "147")
public class OperaLoveSong extends Card {

    public OperaLoveSong() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top two cards of your library. You may play those cards until your next end step",
                        new ExileTopCardMayPlayThisTurnEffect(2, false)
                                .until(Duration.UNTIL_YOUR_NEXT_END_STEP)),
                new ChooseOneEffect.ChooseOneOption(
                        "One or two target creatures each get +2/+0 until end of turn",
                        List.<CardEffect>of(BoostEffect.target(2, 0)),
                        TargetFilters.creature(), null, 1, 2, false, null)
        )));
    }
}
