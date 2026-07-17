package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardsToExileEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "301")
@CardRegistration(set = "5ED", collectorNumber = "385")
public class JestersCap extends Card {

    public JestersCap() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new SearchTargetLibraryForCardsToExileEffect(3)),
                "{2}, {T}, Sacrifice Jester's Cap: Search target player's library for three cards and exile them. Then that player shuffles."
        ));
    }
}
