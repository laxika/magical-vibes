package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "14")
@CardRegistration(set = "SPM", collectorNumber = "235")
@CardRegistration(set = "SPM", collectorNumber = "236")
@CardRegistration(set = "SPM", collectorNumber = "237")
@CardRegistration(set = "SPM", collectorNumber = "238")
@CardRegistration(set = "SPM", collectorNumber = "239")
@CardRegistration(set = "SPM", collectorNumber = "240")
@CardRegistration(set = "SPM", collectorNumber = "241")
public class SpectacularSpiderMan extends Card {

    public SpectacularSpiderMan() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{1}: Spectacular Spider-Man gains flying until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.OWN_CREATURES)
                ),
                "{1}, Sacrifice Spectacular Spider-Man: Creatures you control gain hexproof and indestructible until end of turn."
        ));
    }
}
