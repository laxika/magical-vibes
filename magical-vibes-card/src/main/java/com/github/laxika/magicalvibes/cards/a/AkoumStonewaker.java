package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "140")
public class AkoumStonewaker extends Card {

    public AkoumStonewaker() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayPayManaEffect(
                        "{2}{R}",
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Elemental", 3, 1,
                                CardColor.RED, null, List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of(),
                                false, false, Map.of(), List.of(),
                                false, true, false, 0, Set.of()),
                        "Pay {2}{R} to create a 3/1 red Elemental creature token with trample and haste?"));
    }
}
