package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;

import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "88")
@CardRegistration(set = "KLR", collectorNumber = "96")
public class LostLegacy extends Card {

    public LostLegacy() {
        addSpellEffect(new ChooseCardNameAtResolutionEffect(null, List.of(CardType.ARTIFACT, CardType.LAND), false));
        addSpellEffect(ExileCardsWithSameNameEffect.anyNumberWithChosenName(Subjects.targetPlayer()));
        addSpellEffect(new DrawCardEffect(AmountThisWay.exiledFromHand(), Subjects.targetPlayer()));
    }
}
