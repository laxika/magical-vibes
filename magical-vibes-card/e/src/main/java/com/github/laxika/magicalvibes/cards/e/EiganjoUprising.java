package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "217")
public class EiganjoUprising extends Card {

    public EiganjoUprising() {
        addSpellEffect(new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Samurai", 2, 2, CardColor.WHITE, null,
                List.of(CardSubtype.SAMURAI), Set.of(Keyword.VIGILANCE), Set.of(), false, false,
                List.of(), List.of(), false, false, false, 0,
                Set.of(Keyword.MENACE, Keyword.HASTE)));
        addSpellEffect(new EachOpponentCreatesTokenEffect(new CreateTokenEffect(
                CardType.CREATURE, new Sum(new XValue(), new Fixed(-1)), "Samurai", 2, 2, CardColor.WHITE, null,
                List.of(CardSubtype.SAMURAI), Set.of(Keyword.VIGILANCE), Set.of(), false, false,
                List.of(), List.of(), false, false, false, 0, Set.of())));
    }
}
