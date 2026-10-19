package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.List;
import java.util.UUID;

/**
 * Tempt with Mayhem's tempting offer: each opponent in APNAP order may copy the targeted spell (and may
 * choose new targets for the copy they control); only then does the spell's controller copy it once plus
 * once for each opponent who did, so the controller's copies sit above the opponents' copies.
 *
 * <p>The no-argument form is the card's effect; the other components carry the offer's progress while
 * opponents decide.
 *
 * @param spellSnapshot       the targeted spell, captured when the effect first resolves
 * @param remainingOpponentIds opponents who have yet to decide, in APNAP order
 * @param abilityControllerId the controller of the Tempt with Mayhem spell
 * @param controllerCopies    how many copies the controller makes once every opponent has decided
 */
public record TemptingOfferCopySpellEffect(
        StackEntry spellSnapshot,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        int controllerCopies
) implements CardEffect {

    public TemptingOfferCopySpellEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    public TemptingOfferCopySpellEffect() {
        this(null, null, null, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
