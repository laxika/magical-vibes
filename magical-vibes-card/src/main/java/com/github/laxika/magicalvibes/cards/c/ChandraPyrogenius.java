package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "265")
public class ChandraPyrogenius extends Card {

    public ChandraPyrogenius() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)),
                "+2: Chandra deals 2 damage to each opponent."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetCreatureEffect(4)),
                "\u22123: Chandra deals 4 damage to target creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new DealDamageToTargetAndTheirCreaturesEffect(6)),
                "\u221210: Chandra deals 6 damage to target player or planeswalker and each creature that player or that planeswalker's controller controls.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        "Target must be a player or planeswalker"
                )
        ));
    }
}
