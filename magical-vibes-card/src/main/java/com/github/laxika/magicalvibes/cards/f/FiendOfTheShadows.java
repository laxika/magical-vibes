package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "62")
public class FiendOfTheShadows extends Card {

    public FiendOfTheShadows() {
        // Whenever this creature deals combat damage to a player, that player exiles a card from
        // their hand. You may play that card for as long as it remains exiled.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TargetPlayerExilesFromHandEffect(1, true));

        // Sacrifice a Human: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificePermanentCost(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
                        )),
                        "Sacrifice a Human",
                        false
                ), new RegenerateEffect()),
                "Sacrifice a Human: Regenerate Fiend of the Shadows."
        ));
    }
}
