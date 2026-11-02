package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "92")
@CardRegistration(set = "FDN", collectorNumber = "196")
@CardRegistration(set = "AKR", collectorNumber = "154")
public class FirebrandArcher extends Card {

    public FirebrandArcher() {
        // Whenever you cast a noncreature spell, Firebrand Archer deals 1 damage to each opponent.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))
        ));
    }
}
