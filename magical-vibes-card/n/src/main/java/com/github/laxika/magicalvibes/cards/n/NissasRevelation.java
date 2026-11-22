package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "191")
@CardRegistration(set = "GNT", collectorNumber = "47")
public class NissasRevelation extends Card {

    public NissasRevelation() {
        // Scry 5, then reveal the top card of your library. If it's a creature card, you draw
        // cards equal to its power and you gain life equal to its toughness.
        addSpellEffect(new ScryEffect(5));
        addSpellEffect(new RevealTopCardOfLibraryEffect(Subjects.you()));
        addSpellEffect(new DrawCardEffect(AmountThisWay.revealedPower(new CardTypePredicate(CardType.CREATURE))));
        addSpellEffect(new GainLifeEffect(AmountThisWay.revealedToughness(new CardTypePredicate(CardType.CREATURE))));
    }
}
