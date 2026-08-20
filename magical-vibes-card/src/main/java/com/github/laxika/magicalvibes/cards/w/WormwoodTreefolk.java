package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "92")
public class WormwoodTreefolk extends Card {

    public WormwoodTreefolk() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}",
                List.of(
                        new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.SELF),
                        new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)
                ),
                "{G}{G}: Wormwood Treefolk gains forestwalk until end of turn and deals 2 damage to you."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(
                        new GrantKeywordEffect(Keyword.SWAMPWALK, GrantScope.SELF),
                        new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)
                ),
                "{B}{B}: Wormwood Treefolk gains swampwalk until end of turn and deals 2 damage to you."
        ));
    }
}
