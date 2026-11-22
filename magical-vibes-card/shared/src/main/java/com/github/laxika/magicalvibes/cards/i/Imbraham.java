package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsWithStudyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasStudyCounterPredicate;

public class Imbraham extends Card {

    public Imbraham() {
        addActivatedAbility(new ActivatedAbility(true, "{X}{U}{U}", List.of(
                new ExileTopCardsWithStudyCountersEffect(new XValue()),
                ChooseCardEffect.upTo(Subjects.exiledCards(Subjects.you(), new CardHasStudyCounterPredicate()), 1,
                        "a card you own in exile with a study counter on it"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                "{X}{U}{U}, {T}: Exile the top X cards of your library and put a study counter on each "
                        + "of them. Then you may put a card you own in exile with a study counter on it "
                        + "into your hand."));
    }
}
