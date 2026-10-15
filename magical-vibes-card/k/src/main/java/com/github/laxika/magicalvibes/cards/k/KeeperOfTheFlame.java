package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMoreLifeThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "85")
public class KeeperOfTheFlame extends Card {

    public KeeperOfTheFlame() {
        // {R}, {T}: Choose target opponent who has more life than you do as you activate this
        // ability. This creature deals 2 damage to that player.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)),
                "{R}, {T}: Choose target opponent who has more life than you do as you activate this ability. "
                        + "This creature deals 2 damage to that player.",
                new PlayerPredicateTargetFilter(
                        new PlayerHasMoreLifeThanControllerPredicate(),
                        "Target opponent must have more life than you"
                )
        ));
    }
}
