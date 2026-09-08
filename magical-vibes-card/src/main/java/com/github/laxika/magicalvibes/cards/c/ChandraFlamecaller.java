package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "104")
public class ChandraFlamecaller extends Card {

    public ChandraFlamecaller() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 2, "Elemental", 3, 1,
                        CardColor.RED, null, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), Set.of(),
                        false, false, Map.of(), List.of(), false, true, false, 0, Set.of())),
                "+1: Create two 3/1 red Elemental creature tokens with haste. Exile them at the beginning of the next end step."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DiscardOwnHandThenDrawThatManyEffect(), new DrawCardEffect(1)),
                "0: Discard all the cards in your hand, then draw that many cards plus one."
        ));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new MassDamageEffect(new XValue(), false)),
                "\u2212X: Chandra deals X damage to each creature.",
                null
        ));
    }
}
