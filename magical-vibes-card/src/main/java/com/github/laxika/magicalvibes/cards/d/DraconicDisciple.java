package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "215")
public class DraconicDisciple extends Card {

    public DraconicDisciple() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect("Dragon", 5, 5, CardColor.RED,
                                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())
                ),
                "{7}, {T}, Sacrifice this creature: Create a 5/5 red Dragon creature token with flying."
        ));
    }
}
