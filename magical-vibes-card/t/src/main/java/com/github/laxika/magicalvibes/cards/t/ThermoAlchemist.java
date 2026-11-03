package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "174")
@CardRegistration(set = "INR", collectorNumber = "403")
@CardRegistration(set = "EMN", collectorNumber = "147")
@CardRegistration(set = "MID", collectorNumber = "164")
@CardRegistration(set = "UMA", collectorNumber = "151")
@CardRegistration(set = "SIR", collectorNumber = "180")
@CardRegistration(set = "DBL", collectorNumber = "164")
@CardRegistration(set = "SCD", collectorNumber = "162")
public class ThermoAlchemist extends Card {

    public ThermoAlchemist() {
        // {T}: This creature deals 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{T}: This creature deals 1 damage to each opponent."
        ));

        // Whenever you cast an instant or sorcery spell, untap this creature.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        ))),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
