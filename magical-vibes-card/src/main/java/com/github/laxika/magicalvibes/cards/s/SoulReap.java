package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

/**
 * Soul Reap — {1}{B} Sorcery.
 * Destroy target nongreen creature. Its controller loses 3 life if you've cast another black spell
 * this turn.
 */
@CardRegistration(set = "EVE", collectorNumber = "44")
public class SoulReap extends Card {

    public SoulReap() {
        // The life loss reads the target's controller, so it runs while the creature is still on the
        // battlefield — listed before the destroy (Gloomlance pattern).
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.GREEN)))
                )),
                "Target must be a nongreen creature."
        ))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new ControllerCastAnotherSpellThisTurn(new CardColorPredicate(CardColor.BLACK)),
                        new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER)))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
