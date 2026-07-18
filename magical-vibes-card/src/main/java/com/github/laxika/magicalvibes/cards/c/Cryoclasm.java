package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "195")
public class Cryoclasm extends Card {

    public Cryoclasm() {
        // Destroy target Plains or Island. Cryoclasm deals 3 damage to that land's controller.
        target(new PermanentPredicateTargetFilter(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.PLAINS, CardSubtype.ISLAND)),
                "Target must be a Plains or Island"
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET));
    }
}
