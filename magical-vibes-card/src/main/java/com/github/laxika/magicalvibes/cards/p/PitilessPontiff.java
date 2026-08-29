package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "194")
public class PitilessPontiff extends Card {

    public PitilessPontiff() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GrantKeywordEffect(Set.of(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE), GrantScope.SELF)
                ),
                "{1}, Sacrifice another creature: Pitiless Pontiff gains deathtouch and indestructible until end of turn."
        ));
    }
}
