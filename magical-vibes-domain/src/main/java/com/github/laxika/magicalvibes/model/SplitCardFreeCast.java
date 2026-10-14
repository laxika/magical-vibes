package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * A split card being cast without paying its mana cost from its owner's hand or graveyard. The
 * half is chosen as it is cast, before the spell is put onto the stack (CR 709.3), and only that
 * half is evaluated to see whether it can be cast (CR 709.3a).
 *
 * @param physicalCard              the split card, already removed from {@code sourceZone}
 * @param sourceIndex               the card's position in its zone, so it can return there when
 *                                  no half can be cast
 * @param ownerId                   the owner of the zone the card is cast from
 * @param maxManaValue              the largest mana value the cast half may have
 * @param afterSuccessfulCastEffect effect queued once the spell is cast, or null
 * @param sourcePermanentId         the permanent whose ability lets the card be cast, or null
 */
public record SplitCardFreeCast(UUID controllerId, Card physicalCard, Zone sourceZone, int sourceIndex,
                                UUID ownerId, boolean exileInsteadOfGraveyard, int maxManaValue,
                                CardEffect afterSuccessfulCastEffect, UUID sourcePermanentId) {

    /**
     * The chosen half, prepared for the stack, while its targets are chosen one at a time.
     *
     * @param cardToCast    the runtime copy with only the chosen half's characteristics
     * @param spellEffects  the chosen half's effects
     * @param modeEncoding  the chosen half as an encoded mode selection of the card's modal spell
     * @param chosenTargets the targets chosen so far, in declaration order
     */
    public record Targeting(SplitCardFreeCast cast, Card cardToCast, StackEntryType spellType,
                            List<CardEffect> spellEffects, int xValue, int modeEncoding,
                            List<UUID> chosenTargets) {

        public Targeting {
            spellEffects = List.copyOf(spellEffects);
            chosenTargets = List.copyOf(chosenTargets);
        }
    }
}
