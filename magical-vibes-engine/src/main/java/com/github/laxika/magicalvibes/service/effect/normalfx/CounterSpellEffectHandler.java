package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CounterSpellEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.COUNTER_SPELL, StepOutcome.NONE);
        UUID targetCardId = entry.getTargetId();
        List<UUID> groupTargets = entry.targetsForEffect(effect);
        Card targeting = entry.getTargetingCard();
        boolean boundToGroup = targeting != null && targeting.hasEffectTargetIndex(effect);
        if (boundToGroup && groupTargets.size() > 1) {
            counterEachTarget(gameData, entry, groupTargets, ((CounterSpellEffect) effect).destination());
            return;
        }
        if (targetCardId == null && groupTargets.size() == 1) {
            targetCardId = groupTargets.getFirst();
        }
        if (targetCardId == null) return;

        UUID counterTargetId = targetCardId;
        StackEntry targetOnStack = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(counterTargetId))
                .findFirst()
                .orElse(null);
        if (targetOnStack != null) {
            entry.setCounteredSpellControllerId(targetOnStack.getControllerId());
            entry.setCounteredAbilitySourceId(StackEntryType.ABILITIES.contains(targetOnStack.getEntryType())
                    ? targetOnStack.getSourcePermanentId() : null);
            // The spell as effects see it: a face-down one only has its face-down characteristics (CR 708.4).
            entry.rememberLastKnownPermanentCard(counterTargetId,
                    CastingPermissionService.spellAsEffectsSeeIt(targetOnStack).getCard());
            Card targetingCard = CastingPermissionService.spellAsEffectsSeeIt(targetOnStack).getTargetingCard();
            if (targetingCard == null) {
                // An ability with no source object (the planeswalking ability, CR 901.8) has no mana value.
                entry.setEventValue(0);
            } else {
                int xSymbols = targetingCard.getManaCost() == null
                        ? 0 : new ManaCost(targetingCard.getManaCost()).getXSymbolCount();
                entry.setEventValue(targetingCard.getManaValue() + targetOnStack.getXValue() * xSymbols);
            }
        }

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;
        boolean countered = counter(gameData, entry, targetEntry, ((CounterSpellEffect) effect).destination());
        entry.recordStep(StepKind.COUNTER_SPELL, countered
                ? StepOutcome.ofObjects(List.of(targetCardId), listOfController(targetEntry),
                movedCard(gameData, targetCardId, targetEntry))
                : StepOutcome.NONE);
    }

    /**
     * "Counter up to N target spells": every spell or ability chosen for the effect's target group, each looked up
     * and countered on its own, so one that left the stack or can't be countered is skipped and the others are still
     * countered (CR 608.2b). Records every countered object in one COUNTER_SPELL outcome.
     */
    private void counterEachTarget(GameData gameData, StackEntry entry, List<UUID> targetIds,
                                   CounteredSpellDestination destination) {
        List<UUID> counteredIds = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        List<MovedCard> moved = new ArrayList<>();
        for (UUID targetId : targetIds) {
            StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetId, entry);
            if (targetEntry == null || !counter(gameData, entry, targetEntry, destination)) {
                continue;
            }
            counteredIds.add(targetId);
            for (UUID controller : listOfController(targetEntry)) {
                if (!controllers.contains(controller)) {
                    controllers.add(controller);
                }
            }
            moved.addAll(movedCard(gameData, targetId, targetEntry));
        }
        entry.recordStep(StepKind.COUNTER_SPELL, counteredIds.isEmpty()
                ? StepOutcome.NONE : StepOutcome.ofObjects(counteredIds, controllers, moved));
    }

    /** Counters {@code targetEntry} into {@code destination}; whether it was countered. */
    private boolean counter(GameData gameData, StackEntry entry, StackEntry targetEntry,
                            CounteredSpellDestination destination) {
        rememberTargetPlayerOfCounteredAbility(gameData, targetEntry);
        return switch (destination) {
            case GRAVEYARD -> counterSupport.counterSpell(gameData, entry, targetEntry);
            case EXILE -> counterSupport.counterSpellAndExile(gameData, entry, targetEntry);
            case HAND -> counterSupport.counterSpellAndPutInHand(gameData, entry, targetEntry);
            case LIBRARY_TOP -> {
                counterSupport.counterSpellAndPutOnTopOfLibrary(gameData, entry, targetEntry);
                yield true;
            }
            case LIBRARY_BOTTOM -> {
                counterSupport.counterSpellAndPutOnBottomOfLibrary(gameData, entry, targetEntry);
                yield true;
            }
            case LIBRARY_TOP_OR_BOTTOM -> {
                counterOntoChosenLibraryEnd(gameData, entry, targetEntry);
                yield true;
            }
        };
    }

    /**
     * The card the countered spell became in the public zone it was put into, its graveyard or exile, which "that
     * card" after the counter names ({@code cardsThisWay(COUNTER_SPELL)}); none for a copy, an ability or a card put
     * into a hidden zone.
     */
    private List<MovedCard> movedCard(GameData gameData, UUID objectId, StackEntry countered) {
        if (countered.isCopy() || countered.getEntryType().isAbility() || countered.getPhysicalCard() == null) {
            return List.of();
        }
        UUID cardId = countered.getPhysicalCard().getId();
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled != null) {
            return List.of(new MovedCard(objectId, cardId, Zone.EXILE, exiled.ownerId()));
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        return graveyardOwnerId == null ? List.of()
                : List.of(new MovedCard(objectId, cardId, Zone.GRAVEYARD, graveyardOwnerId));
    }

    /**
     * A linked "that player" ability refers to the player chosen when the first ability was put on the
     * stack, so countering that ability (Stifle) must not lose the choice (Laquatus's Champion).
     */
    private void rememberTargetPlayerOfCounteredAbility(GameData gameData, StackEntry countered) {
        UUID playerId = countered.getTargetId();
        UUID sourcePermanentId = countered.getSourcePermanentId();
        if (sourcePermanentId == null || playerId == null || !gameData.playerIds.contains(playerId)
                || countered.getEffectsToResolve().stream().noneMatch(RememberTargetPlayerEffect.class::isInstance)) {
            return;
        }
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.getId().equals(sourcePermanentId)) {
                permanent.setRememberedTargetPlayerId(playerId);
            }
        });
    }

    /**
     * Hinder: the card is countered onto the top of its owner's library, then its controller picks
     * whether it stays there or goes to the bottom.
     */
    private void counterOntoChosenLibraryEnd(GameData gameData, StackEntry entry, StackEntry targetEntry) {
        UUID ownerId = targetEntry.getOwnerId();
        Card countered = counterSupport.counterSpellOntoLibraryPendingEndChoice(gameData, entry, targetEntry);
        if (countered == null) {
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CounteredSpellLibraryDestinationChoice(
                entry.getControllerId(), ownerId, countered.getId(), countered.getName()));
    }

    private static List<UUID> listOfController(StackEntry countered) {
        return countered.getControllerId() == null ? List.of() : List.of(countered.getControllerId());
    }
}
