package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "STX", collectorNumber = "173")
public class CulminationOfStudies extends Card {

    public CulminationOfStudies() {
        addSpellEffect(ExileTopCardOfOwnLibraryEffect.of(Subjects.you(), new XValue()));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(
                AmountThisWay.exiledCards(new CardTypePredicate(CardType.LAND))));
        addSpellEffect(new DrawCardEffect(AmountThisWay.exiledCards(new CardColorPredicate(CardColor.BLUE))));
        addSpellEffect(DealDamageEffect.to(Subjects.eachOpponent(),
                AmountThisWay.exiledCards(new CardColorPredicate(CardColor.RED))));
    }
}
