package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsWithStudyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnStudyCounterCardEffect;

import java.util.List;

public class Imbraham extends Card {

    public Imbraham() {
        addActivatedAbility(new ActivatedAbility(true, "{X}{U}{U}", List.of(
                new ExileTopCardsWithStudyCountersEffect(new XValue()),
                new ReturnStudyCounterCardEffect()),
                "{X}{U}{U}, {T}: Exile the top X cards of your library and put a study counter on each "
                        + "of them. Then you may put a card you own in exile with a study counter on it "
                        + "into your hand."));
    }
}
