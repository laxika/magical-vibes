package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "160")
public class GolemFoundry extends Card {

    public GolemFoundry() {
        // Whenever you cast an artifact spell, you may put a charge counter on Golem Foundry.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ARTIFACT),
                        List.of(new PutChargeCounterOnSelfEffect())),
                "Put a charge counter on Golem Foundry?"
        ));

        // Remove three charge counters from Golem Foundry: Create a 3/3 colorless Golem artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(3),
                        new CreateTokenEffect("Golem", 3, 3, null,
                                List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "Remove three charge counters from Golem Foundry: Create a 3/3 colorless Golem artifact creature token."
        ));
    }
}
