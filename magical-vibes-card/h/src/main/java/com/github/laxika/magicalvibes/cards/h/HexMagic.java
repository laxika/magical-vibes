package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MSH", collectorNumber = "133")
public class HexMagic extends Card {

    public HexMagic() {
        addSpellEffect(SequenceEffect.of(
                ExileTargetPlayerHandEffect.handsOf(Subjects.you()),
                new DrawCardEffect(new AmountThisWay(StepKind.EXILE)),
                AllowPlayCardsFromExileEffect.untilEndOfYourNextTurn(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
