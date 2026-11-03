package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "27")
@CardRegistration(set = "DSC", collectorNumber = "56")
public class GleefulArsonist extends Card {

    public GleefulArsonist() {
        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.TRIGGERING_PLAYER));
    }
}
