package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "48")
public class VesselOfEphemera extends Card {

    public VesselOfEphemera() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(2, "Spirit", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of())
                ),
                "{2}{W}, Sacrifice this enchantment: Create two 1/1 white Spirit creature tokens with flying."
        ));
    }
}
