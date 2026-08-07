package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerDamagedBySourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "150")
public class WickedAkuba extends Card {

    public WickedAkuba() {
        // "{B}: Target player dealt damage by this creature this turn loses 1 life." — no tap, and
        // the target is restricted to a player this permanent already damaged this turn (combat or
        // noncombat); the restriction is rechecked on resolution.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                "{B}: Target player dealt damage by Wicked Akuba this turn loses 1 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerDamagedBySourceThisTurnPredicate(),
                        "Target player must have been dealt damage by this creature this turn"
                )
        ));
    }
}
