package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "113")
@CardRegistration(set = "DDJ", collectorNumber = "5")
@CardRegistration(set = "GK1", collectorNumber = "38")
public class Gelectrode extends Card {

    public Gelectrode() {
        // {T}: This creature deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Gelectrode deals 1 damage to any target."
        ));

        // Whenever you cast an instant or sorcery spell, you may untap this creature.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        ))),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.SELF), "Untap Gelectrode?"));
    }
}
