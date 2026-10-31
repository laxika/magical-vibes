package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "212")
public class PracticalResearch extends Card {

    public PracticalResearch() {
        addSpellEffect(new DrawCardEffect(4));
        addSpellEffect(new DiscardTwoUnlessCardTypeEffect(Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
