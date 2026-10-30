package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtNextUpkeep;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.PendingTruthOrTaleCardChoice;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPortalPileSearch;
import com.github.laxika.magicalvibes.model.RetetherAuraChoiceRequest;
import com.github.laxika.magicalvibes.model.RetetherAuraPlacement;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasSubtype;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;

/**
 * Shared graveyard return/exile helpers used by every normal Graveyard Return effect handler
 * and by input handlers (graveyard choice, may ability pile separation).
 *
 * <p>Extracted verbatim from {@code GraveyardReturnResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraveyardReturnSupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.CardRevealService cardRevealService;
    private final LifeSupport lifeSupport;
    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentCounterSupport permanentCounterSupport;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final ConditionEvaluationService conditionEvaluationService;
    private final com.github.laxika.magicalvibes.service.effect.AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final EquipSupport equipSupport;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport graveyardTargetingSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final MakeChosenPermanentAttackingEffectHandler makeChosenPermanentAttackingEffectHandler;

    /**
     * Resolves a {@link ReturnCardFromGraveyardEffect} by returning one or more cards from a graveyard
     * to the controller's hand or battlefield. Handles three resolution paths:
     * <ol>
     *   <li>Pre-targeted: the card was already targeted during casting (includes aura attachment).</li>
     *   <li>Return all: returns every matching card without player choice.</li>
     *   <li>Search and choose: prompts the controller to pick a card from their own or all graveyards.</li>
     * </ol>
     * Fizzles if targeted cards are no longer valid. May also grant life equal to the returned card's
     * mana value when the effect specifies it.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the graveyard return effect configuration
     */

    /** Appends {@code cards} to {@code builder} as comma-separated card segments. */
    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }

    /**
     * Whether {@code auraCard} could legally enchant {@code host} (CR 303.4a — an Aura's enchant
     * ability restricts what it can be attached to). Auras without an explicit target filter
     * default to "enchant creature".
     */
    private boolean canEnchant(GameData gameData, Card auraCard, UUID auraControllerId, Permanent host) {
        return auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, host);
    }

    /**
     * Exiles the source card from its owner's graveyard when the effect declares
     * {@code exileSourceFromGraveyard} ("exile it, then …" — Moldgraf Monstrosity, Sylvan
     * Hierophant). A no-op when the source card has already left the graveyard.
     */
    private void exileSourceFromGraveyardIfRequested(GameData gameData, StackEntry entry,
                                                     ReturnCardFromGraveyardEffect effect,
                                                     UUID controllerId, UUID sourceCardId) {
        if (!effect.exileSourceFromGraveyard() || sourceCardId == null) {
            return;
        }
        // A card that left the graveyard and came back is a new object the trigger cannot exile.
        if (entry != null && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(sourceCardId) != entry.getTriggeringCardGraveyardEntryVersion()) {
            return;
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCardId);
        List<Card> graveyard = ownerId == null ? null : gameData.playerGraveyards.get(ownerId);
        if (graveyard == null) {
            return;
        }
        Card sourceCard = graveyard.stream()
                .filter(c -> c.getId().equals(sourceCardId))
                .findFirst()
                .orElse(null);
        if (sourceCard == null) {
            return;
        }
        graveyard.remove(sourceCard);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, ownerId, sourceCard);
        exileService.exileCard(gameData, ownerId, sourceCard);
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " exiles ", sourceCard, " from graveyard."));
        log.info("Game {} - {} exiles {} from graveyard", gameData.id, playerName, sourceCard.getName());
    }

    public void resolvePreTargeted(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                    UUID controllerId, UUID sourceCardId) {
        resolvePreTargetedById(gameData, entry, effect, controllerId, sourceCardId, entry.getTargetId());
    }

    public void resolvePreTargetedById(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                        UUID controllerId, UUID sourceCardId, UUID targetCardId) {
        // "Exile it, then return another target creature card …" (Sylvan Hierophant): the self-exile
        // happens first and is not contingent on the return actually working.
        exileSourceFromGraveyardIfRequested(gameData, entry, effect, controllerId, sourceCardId);

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());
        UUID targetOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);

        if (!matchesReturnCardFilter(gameData, entry, effect, targetCard, sourceCardId)
                || (effect.targetNotPutIntoGraveyardThisCombat()
                && targetOwnerId != null
                && gameData.cardsPutIntoGraveyardThisCombat
                        .getOrDefault(targetOwnerId, Set.of()).contains(targetCardId))) {
            String fizzleLog = entry.getDescription() + " fizzles (target " + filterLabel + " is no longer in a graveyard).";
            gameLogService.append(gameData, GameLog.text(fizzleLog));
            return;
        }

        // Grafdigger's Cage etc.: a card that can't enter the battlefield from a graveyard is never removed
        // from it, so the target stays in the graveyard it is in.
        if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD
                && isCardBlockedFromEnteringFromZone(gameData, targetCard, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.cardThen(targetCard,
                    " can't enter the battlefield from a graveyard; it stays in the graveyard."));
            if (effect.perpetualBattlefieldEffectGrants() != null) {
                PerpetualCardBattlefieldEffectSupport.remember(
                        gameData, targetCard, effect.perpetualBattlefieldEffectGrants());
            }
            return;
        }

        // Mandatory attach-to-source path (Hakim, Loreweaver): the Aura is put onto the battlefield
        // attached to the source permanent itself, so there is no host to choose. The target is
        // illegal — and the ability fizzles — when the Aura can't legally enchant the source.
        if (effect.attachToSource() && effect.destination() == GraveyardChoiceDestination.BATTLEFIELD) {
            Permanent sourcePermanent = entry.getSourcePermanentId() == null ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            boolean isEquipment = targetCard.getSubtypes().contains(CardSubtype.EQUIPMENT);
            if (isEquipment) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
                Permanent equipment = putCardOntoBattlefield(gameData, controllerId, targetCard,
                        null, null, effect.enterTapped(), false, null, false);
                if (sourcePermanent != null && effect.attachToSourceOptional()) {
                    gameData.interaction.setPendingEquipmentAttach(equipment.getId(), sourcePermanent.getId());
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            sourcePermanent.getCard(), controllerId, List.of(),
                            sourcePermanent.getCard().getName() + " — Attach " + targetCard.getName()
                                    + " to " + sourcePermanent.getCard().getName() + "?"));
                    playerInputService.processNextMayAbility(gameData);
                } else if (sourcePermanent != null) {
                    equipSupport.attachEquipment(gameData, equipment, sourcePermanent);
                }
                return;
            }
            if (sourcePermanent == null
                    || !canEnchant(gameData, targetCard, controllerId, sourcePermanent)) {
                gameLogService.append(gameData, GameLog.textCardText(entry.getDescription()
                        + " fizzles (", targetCard, " can't be attached)."));
                return;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
            Permanent attachedPermanent = new Permanent(targetCard);
            attachedPermanent.setAttachedTo(sourcePermanent.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, attachedPermanent);
            gameLogService.append(gameData, GameLog.builder().card(targetCard)
                    .text(" enters the battlefield attached to ").card(sourcePermanent.getCard()).text(".").build());
            log.info("Game {} - {} enters the battlefield attached to {}", gameData.id,
                    targetCard.getName(), sourcePermanent.getCard().getName());
            return;
        }

        if (effect.chooseAuraAttachment() && effect.destination() == GraveyardChoiceDestination.BATTLEFIELD
                && targetCard.isAura()
                && beginGraveyardCardAuraAttachment(gameData, entry, effect, targetCard, targetOwnerId, controllerId)) {
            return;
        }

        if (effect.chooseAuraAttachment() && effect.destination() == GraveyardChoiceDestination.BATTLEFIELD
                && targetCard.isAura()) {
            UUID auraControllerId = effect.underOwnersControl() && targetOwnerId != null
                    ? targetOwnerId : controllerId;
            List<UUID> attachTargetIds = new ArrayList<>();
            for (UUID battlefieldPlayerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(battlefieldPlayerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (auraAttachmentService.canEnchant(gameData, targetCard, auraControllerId, permanent)) {
                        attachTargetIds.add(permanent.getId());
                    }
                }
            }

            List<UUID> attachPlayerIds = new ArrayList<>();
            if (targetCard.isEnchantPlayer()) {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (auraAttachmentService.canEnchantPlayer(gameData, targetCard, auraControllerId, playerId)) {
                        attachPlayerIds.add(playerId);
                    }
                }
            }

            if (attachTargetIds.isEmpty() && attachPlayerIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.textCardText(entry.getDescription() + " leaves ",
                        targetCard, " in the graveyard because it cannot legally enchant anything."));
                return;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
            gameData.interaction.setPendingAuraCard(targetCard);
            gameData.interaction.setPendingAuraOwnerId(auraControllerId);
            gameData.interaction.setPendingAuraGraveyardReturn(effect);
            playerInputService.beginAnyTargetChoice(gameData, auraControllerId, attachTargetIds, attachPlayerIds,
                    "Choose a permanent or player for " + targetCard.getName() + " to enchant.");
            return;
        }

        // Aura or Equipment attachment path
        if (effect.attachmentTarget() != null) {
            Permanent equipment = targetCard.getSubtypes().contains(CardSubtype.EQUIPMENT)
                    ? new Permanent(targetCard) : null;
            List<Permanent> controllerBf = gameData.playerBattlefields.get(controllerId);
            List<UUID> attachTargetIds = new ArrayList<>();
            if (controllerBf != null) {
                for (Permanent p : controllerBf) {
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.attachmentTarget())
                            && (equipment != null
                                    ? equipSupport.canAttachEquipment(gameData, equipment, p)
                                    : auraAttachmentService.canEnchant(gameData, targetCard, controllerId, p))) {
                        attachTargetIds.add(p.getId());
                    }
                }
            }

            if (attachTargetIds.isEmpty() && equipment == null) {
                String fizzleLog = entry.getDescription() + " fizzles (no legal creatures to attach to).";
                gameLogService.append(gameData, GameLog.text(fizzleLog));
                return;
            }

            if (!attachTargetIds.isEmpty()) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
                gameData.interaction.setPendingAuraCard(targetCard);

                playerInputService.beginPermanentChoice(gameData, controllerId, attachTargetIds,
                        "Choose a creature you control to attach " + targetCard.getName() + " to.");
                return;
            }
            // An Equipment with no creature to attach to still returns, unattached (One Last Job ruling,
            // 2024-04-12); an Aura has nothing to enchant and stays in the graveyard.
        }

        if (effect.destination() == GraveyardChoiceDestination.EXILE) {
            exileCardFromAnyGraveyard(gameData, targetCard.getId(), targetCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " exiles ", targetCard, " from a graveyard."));
            return;
        }

        // A card returned to HAND or to the top of a library always goes to its owner's zone
        // (only BATTLEFIELD returns can put a card under a non-owner's control). Battlefield
        // returns also use the owner when requested. Resolve the graveyard owner before removal.
        UUID destinationPlayerId = controllerId;
        if (effect.destination() == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY
                || effect.destination() == GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY
                || effect.destination() == GraveyardChoiceDestination.HAND
                || (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD && effect.underOwnersControl())) {
            if (targetOwnerId != null) {
                destinationPlayerId = targetOwnerId;
            }
        }

        // A card that returns to the battlefield leaves the graveyard as that move happens, so the
        // "leaves your graveyard" notifications wait until it has entered (it is then a new object that may be
        // judged against effects such as Hushbringer's).
        boolean returningToBattlefield = effect.destination() == GraveyardChoiceDestination.BATTLEFIELD;
        Permanent returnedPermanent = null;
        boolean entersWithPlusOneCounters;
        boolean hasteOrExileEntry;
        boolean entersWithEffectCounters;
        if (returningToBattlefield) {
            graveyardService.beginGraveyardLeaveBatch(gameData);
        }
        try {
            permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

            PendingGraveyardReturnBatch openBatch = gameData.pendingGraveyardReturnBatch;
            if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD
                    && openBatch != null && openBatch.closedByEffect()) {
                // Liliana, the Necromancer's -7: the chosen cards wait until a later effect puts them all onto the
                // battlefield together (CR 603.6a).
                gameData.pendingGraveyardReturnBatch = openBatch.add(targetCard,
                        targetOwnerId != null ? targetOwnerId : controllerId);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(controllerId) + " chooses ", targetCard, " from a graveyard."));
                return;
            }

            // Unconditional "+1/+1 counters" are put on the card as it enters, so entry triggers see them.
            entersWithPlusOneCounters = entersWithPlusOneCounters(gameData, targetCard, targetOwnerId, effect)
                    || entersWithConditionalPlusOneCounters(gameData, entry, destinationPlayerId, effect);
            hasteOrExileEntry = effect.grantHaste() || effect.exileAtEndStep()
                    || effect.exileAtYourNextEndStep() || effect.sacrificeAtEndStep();
            // The haste/exile entry path doesn't place counters, so the riders below still do.
            entersWithEffectCounters = !entersWithPlusOneCounters && !hasteOrExileEntry
                    && effect.enterWithCounter() != null && effect.enterWithCounterCount() > 0;
            if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD) {
                if (effect.grantHaste() || effect.exileAtEndStep() || effect.exileAtYourNextEndStep()
                        || effect.sacrificeAtEndStep()) {
                    returnedPermanent = putCardOntoBattlefieldWithHasteAndExile(gameData, destinationPlayerId, targetCard,
                            effect.grantHaste(), effect.exileAtEndStep(), effect.sacrificeAtEndStep(),
                            effect.exileIfLeavesBattlefield(), effect.enterTapped(), effect.enterAttacking(),
                            effect.exileAtYourNextEndStep(), losesAllAbilitiesBeforeEntering(effect), entry.getCard());
                    limitHasteToThisTurn(effect, returnedPermanent);
                } else {
                    returnedPermanent = putCardOntoBattlefield(gameData, destinationPlayerId, targetCard,
                            effect.grantColor(), effect.grantSubtype(), effect.enterTapped(), effect.enterAttacking(),
                            entersWithEffectCounters ? effect.enterWithCounter()
                                    : entersWithPlusOneCounters ? CounterType.PLUS_ONE_PLUS_ONE : null,
                            effect.grantIndestructible(), losesAllAbilitiesBeforeEntering(effect),
                            entersWithEffectCounters ? effect.enterWithCounterCount()
                                    : entersWithPlusOneCounters ? effect.plusOneCounterCount() : 0,
                            permanent -> {
                                permanent.setEnteredFromGraveyardOwnerId(targetOwnerId);
                                if (effect.battlefieldEffectGrants() != null) {
                                    for (CardEffect grant : effect.battlefieldEffectGrants()) {
                                        if (grant instanceof GrantColorEffect || grant instanceof SetCardTypesEffect
                                                || grant instanceof GrantCardTypeEffect
                                                || grant instanceof GrantSubtypeEffect
                                                || grant instanceof SetBasePowerToughnessEffect) {
                                            gameData.addFloatingEffect(new FloatingContinuousEffect(
                                                    UUID.randomUUID(), entry.getCard().getName(),
                                                    entry.getSourcePermanentId(), entry.getControllerId(), grant,
                                                    permanent.getId(), null, null,
                                                    effect.battlefieldEffectGrantDuration() == null
                                                            ? EffectDuration.PERMANENT
                                                            : effect.battlefieldEffectGrantDuration(), 0));
                                        }
                                    }
                                }
                                if (effect.grantSubtypes() != null) {
                                    permanent.getGrantedSubtypes().addAll(effect.grantSubtypes());
                                }
                            });
                }
            } else {
                moveCardToDestination(gameData, destinationPlayerId, targetCard, effect.destination(),
                        effect.grantColor(), effect.grantSubtype(), effect.enterTapped());
            }
        } finally {
            if (returningToBattlefield) {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
        }

        if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD) {
            if (returnedPermanent != null && effect.enterAttacking()) {
                UUID attackTargetId = entry.getAttackedTargetId();
                if (attackTargetId == null && entry.getSourcePermanentId() != null) {
                    Permanent sourcePermanent = gameQueryService.findPermanentById(
                            gameData, entry.getSourcePermanentId());
                    if (sourcePermanent != null) {
                        attackTargetId = sourcePermanent.getAttackTarget();
                    }
                }
                if (attackTargetId == null && effect.source() == GraveyardSearchScope.OPPONENT_GRAVEYARD) {
                    attackTargetId = targetOwnerId;
                }
                returnedPermanent.setAttackTarget(attackTargetId);
            }
            applyBattlefieldReturnRiders(gameData, destinationPlayerId, targetCard, effect, entry, targetOwnerId,
                    entersWithPlusOneCounters && returnedPermanent != null,
                    entersWithEffectCounters && returnedPermanent != null);
            trackAndLinkReanimatedPermanent(gameData, entry, effect, destinationPlayerId, targetCard, targetOwnerId);
            if (returnedPermanent != null
                    && effect.createTokensIfSubtype() != null
                    && effect.createTokensEffect() != null) {
                // "If it's an Elf" checks the returned object itself, so a copy that entered without the
                // subtype (e.g. Moritte entering as a noncreature copy) does not qualify.
                if (gameQueryService.findPermanentById(gameData, returnedPermanent.getId()) == null
                        && gameData.interaction.isAwaitingInput()) {
                    // Its enter-as-a-copy choice is still open, so the subtype is checked once it is answered.
                    entry.setTriggeringPermanentId(returnedPermanent.getId());
                    entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                            ConditionalEffect.unless(new TriggeringPermanentHasSubtype(effect.createTokensIfSubtype()),
                                    effect.createTokensEffect())));
                } else if (gameQueryService.hasEffectiveSubtype(
                        gameData, returnedPermanent, effect.createTokensIfSubtype())) {
                    createTokenEffectHandler.resolve(gameData, entry, effect.createTokensEffect());
                }
            }
        }

        if (effect.returnToHandAtEndStep() && returnedPermanent != null) {
            gameData.queueDelayedAction(new DelayedPermanentAction(
                    returnedPermanent.getId(), DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP));
        }

        if (effect.returnToHandAtYourNextEndStep() && returnedPermanent != null) {
            gameData.queueDelayedAction(new DelayedPermanentAction(
                    returnedPermanent.getId(), DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP,
                    controllerId));
        }

        if (effect.exileAtNextUpkeep() && returnedPermanent != null) {
            gameData.queueDelayedAction(new ExilePermanentAtNextUpkeep(
                    controllerId, returnedPermanent.getId(), entry.getCard()));
        }

        if (effect.gainLifeEqualToManaValue()) {
            applyLifeGainEqualToManaValue(gameData, controllerId, targetCard);
        }

        if (effect.gainLifeEqualToReturnedToughness() && returnedPermanent != null) {
            applyLifeGainEqualToReturnedToughness(gameData, controllerId, returnedPermanent);
        }

        if (effect.loseLifeEqualToManaValue()) {
            applyLifeLossEqualToManaValue(gameData, entry, controllerId, targetCard);
        }
        if (effect.grantSourceHasteIfSubtype() != null && entry.getSourcePermanentId() != null
                && (gameQueryService.cardHasSubtype(targetCard, effect.grantSourceHasteIfSubtype(), gameData,
                controllerId) || targetCard.hasKeyword(Keyword.CHANGELING))) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (sourcePermanent != null) {
                sourcePermanent.getGrantedKeywords().add(Keyword.HASTE);
                gameLogService.append(gameData, GameLog.cardThen(sourcePermanent.getCard(),
                        " gains haste until end of turn."));
            }
        }
        if (returnedPermanent != null && effect.enterAttacking()
                && attacksSameOpponentAsAbilitySource(gameData, entry, effect, targetOwnerId)) {
            // CR 508.4: the effect already specified what the creature attacks ("attacking that player").
            returnedPermanent.enterAttacking(true);
            returnedPermanent.setAttackedOrBlockedSinceLastUpkeep(true);
            returnedPermanent.setAttackTarget(targetOwnerId);
        } else if (returnedPermanent != null && effect.enterAttacking()) {
            makeChosenPermanentAttackingEffectHandler.resolve(gameData, entry,
                    new com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect(returnedPermanent.getId()));
        }
    }

    /**
     * True when the returned creature comes from an opponent's graveyard on an ability whose attacking
     * source is already attacking that same opponent, so "enters tapped and attacking that player"
     * leaves nothing for the controller to choose.
     */
    private boolean attacksSameOpponentAsAbilitySource(GameData gameData, StackEntry entry,
                                                       ReturnCardFromGraveyardEffect effect, UUID targetOwnerId) {
        if (effect.source() != GraveyardSearchScope.OPPONENT_GRAVEYARD || targetOwnerId == null
                || entry.getSourcePermanentId() == null) {
            return false;
        }
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return sourcePermanent != null && sourcePermanent.isAttacking()
                && targetOwnerId.equals(sourcePermanent.getAttackTarget());
    }

    private boolean matchesReturnCardFilter(GameData gameData, StackEntry entry,
                                             ReturnCardFromGraveyardEffect effect, Card card,
                                             UUID sourceCardId) {
        if (card == null) {
            return false;
        }
        if ((card.isToken() || gameData.dynamicTokenCardIds.contains(card.getId())) && !card.isTokenCard()) {
            return false;
        }
        if (effect.requiresPowerAtMostSacrificedPower()) {
            Integer power = gameQueryService.getEffectiveCardPower(gameData, card);
            if (power == null || power > entry.getSacrificedPower()) {
                return false;
            }
        }
        if (effect.requiresManaValueEqualsX()
                && card.getManaValue() != effect.requiredManaValue(entry.getXValue())) {
            return false;
        }
        if (effect.requiresManaValueAtMostX()
                && card.getManaValue() > effect.requiredManaValue(entry.getXValue())) {
            return false;
        }
        if (effect.dynamicMaxManaValue() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            int maximum = amountEvaluationService.evaluate(gameData, effect.dynamicMaxManaValue(),
                    com.github.laxika.magicalvibes.service.effect.AmountContext.forStackEntry(entry, source));
            if (card.getManaValue() > maximum) {
                return false;
            }
        }
        UUID cardOwnerId = card.getOwnerId() != null
                ? card.getOwnerId()
                : gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (effect.targetPutIntoGraveyardFromAnywhereThisTurn()
                && (cardOwnerId == null
                || !gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                .getOrDefault(cardOwnerId, Set.of()).contains(card.getId()))) {
            return false;
        }
        if (effect.targetPutIntoGraveyardFromLibraryThisTurn()
                && (cardOwnerId == null
                || !gameData.cardsPutIntoGraveyardFromLibraryThisTurn
                .getOrDefault(cardOwnerId, Set.of()).contains(card.getId()))) {
            return false;
        }
        if (effect.targetMilledThisTurn()
                && (cardOwnerId == null
                || !gameData.cardsMilledThisTurn
                .getOrDefault(cardOwnerId, Set.of()).contains(card.getId()))) {
            return false;
        }
        if (effect.targetDiscardedOrPutIntoGraveyardFromLibraryThisTurn()
                && (cardOwnerId == null
                || (!gameData.cardsDiscardedOrCycledThisTurn
                .getOrDefault(cardOwnerId, Set.of()).contains(card.getId())
                && !gameData.cardsPutIntoGraveyardFromLibraryThisTurn
                .getOrDefault(cardOwnerId, Set.of()).contains(card.getId())))) {
            return false;
        }
        if (effect.sourceChosenSubtype()) {
            CardSubtype chosenSubtype = findSourceChosenSubtype(gameData, entry, sourceCardId);
            return chosenSubtype != null
                    && (card.hasKeyword(Keyword.CHANGELING)
                    || gameQueryService.cardHasSubtype(card, chosenSubtype, gameData, cardOwnerId));
        }
        if (effect.filter() instanceof com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate
                && entry.getDiscardedCardSnapshot() != null) {
            // The card exiled by this activation, snapshotted at activation, so a later activation
            // that re-imprints the source does not change what this one shares a type with.
            Card exiledByActivation = entry.getDiscardedCardSnapshot();
            return java.util.Arrays.stream(CardType.values())
                    .anyMatch(type -> gameQueryService.cardHasType(card, type, gameData, cardOwnerId)
                            && gameQueryService.cardHasType(exiledByActivation, type, gameData,
                            exiledByActivation.getOwnerId()));
        }
        return effect.filter() == null
                || predicateEvaluationService.matchesCardPredicate(
                card, effect.filter(), sourceCardId, gameData, cardOwnerId,
                entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getXValue(), entry.getSourcePermanentSnapshot());
    }

    private CardSubtype findSourceChosenSubtype(GameData gameData, StackEntry entry, UUID sourceCardId) {
        if (entry.getSourcePermanentSnapshot() != null) {
            return entry.getSourcePermanentSnapshot().getChosenSubtype();
        }
        if (sourceCardId == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(sourceCardId)
                        || permanent.getOriginalCard().getId().equals(sourceCardId)) {
                    return permanent.getChosenSubtype();
                }
            }
        }
        return null;
    }

    /**
     * Post-processes a pre-targeted graveyard-to-battlefield return. A card reanimated out of another
     * player's graveyard entered under a non-owner's control, so its original owner is recorded (it must
     * go to that player's graveyard when it dies). When the effect asks for it, the new permanent is also
     * bonded to the source permanent via {@code chosenPermanentId} so a later
     * {@link com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect} trigger can find it.
     */
    private void trackAndLinkReanimatedPermanent(GameData gameData, StackEntry entry,
                                                 ReturnCardFromGraveyardEffect effect, UUID controllerId,
                                                 Card card, UUID graveyardOwnerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        Permanent entered = null;
        for (Permanent p : battlefield) {
            if (p.getCard().getId().equals(card.getId())) {
                entered = p;
                break;
            }
        }
        if (entered == null) {
            return;
        }

        if (graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
            trackStolenCreature(gameData, entered.getId(), controllerId, graveyardOwnerId);
        }

        if (effect.linkToSource() && entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                source.setChosenPermanentId(entered.getId());
            }
        }
    }

    /**
     * Applies optional battlefield-entry riders from a {@link ReturnCardFromGraveyardEffect}
     * (mannequin counter, generic enter-with counter, exile-if-leaves replacement, exile-if-dying replacement, granted cumulative
     * upkeep, +1/+1 counters — unconditional when both {@code plusOneCountersIfSubtype} and
     * {@code plusOneCountersIfCondition} are null, else gated by whichever is set).
     */
    private void applyBattlefieldReturnRiders(GameData gameData, UUID controllerId, Card card,
                                              ReturnCardFromGraveyardEffect effect) {
        applyBattlefieldReturnRiders(gameData, controllerId, card, effect, null, controllerId);
    }

    private void applyBattlefieldReturnRiders(GameData gameData, UUID controllerId, Card card,
                                              ReturnCardFromGraveyardEffect effect, StackEntry entry,
                                              UUID cardOwnerId) {
        applyBattlefieldReturnRiders(gameData, controllerId, card, effect, entry, cardOwnerId, false);
    }

    private void applyBattlefieldReturnRiders(GameData gameData, UUID controllerId, Card card,
                                              ReturnCardFromGraveyardEffect effect, StackEntry entry,
                                              UUID cardOwnerId, boolean plusOneCountersAlreadyPlaced) {
        applyBattlefieldReturnRiders(gameData, controllerId, card, effect, entry, cardOwnerId,
                plusOneCountersAlreadyPlaced, false);
    }

    private void applyBattlefieldReturnRiders(GameData gameData, UUID controllerId, Card card,
                                              ReturnCardFromGraveyardEffect effect, StackEntry entry,
                                              UUID cardOwnerId, boolean plusOneCountersAlreadyPlaced,
                                              boolean enterWithCounterAlreadyPlaced) {
        boolean exiledCostSubtypeMatches = effect.plusOneCountersIfExiledCostCardHasSubtype() != null
                && entry != null
                && entry.getExiledCostCardSnapshot() != null
                && entry.getExiledCostCardSnapshot().getSubtypes() != null
                && entry.getExiledCostCardSnapshot().getSubtypes()
                .contains(effect.plusOneCountersIfExiledCostCardHasSubtype());
        boolean plusOneCounters = !plusOneCountersAlreadyPlaced && effect.plusOneCounterCount() > 0
                && (effect.plusOneCountersIfExiledCostCardHasSubtype() == null
                || exiledCostSubtypeMatches)
                && (effect.plusOneCountersIfCardType() == null
                || effect.plusOneCountersIfCardType() == CardType.CREATURE
                || card.hasType(effect.plusOneCountersIfCardType()))
                && (effect.plusOneCountersIfCondition() == null
                || conditionEvaluationService.isMet(gameData, effect.plusOneCountersIfCondition(),
                entry == null
                        ? ConditionContext.forCasting(controllerId)
                        : ConditionContext.forStackEntry(entry)));
        boolean exiledCostCounter = exiledCostSubtypeMatches
                && effect.counterIfExiledCostCardHasSubtype() != null
                && effect.counterCountIfExiledCostCardHasSubtype() > 0;
        boolean enterWithCounter = !enterWithCounterAlreadyPlaced
                && effect.enterWithCounter() != null && effect.enterWithCounterCount() > 0;
        boolean enterWithCounters = effect.enterWithCounters() != null && !effect.enterWithCounters().isEmpty();
        if (!effect.enterWithMannequinCounter()
                && !enterWithCounter
                && !enterWithCounters
                && !effect.exileIfLeavesBattlefield()
                && !effect.exileIfDying()
                && !effect.unearth()
                && !plusOneCounters
                && !exiledCostCounter
                && (effect.grantKeywords() == null || effect.grantKeywords().isEmpty())
                && !effect.grantHasteUntilNextTurn()
                && (effect.grantSubtypes() == null || effect.grantSubtypes().isEmpty())
                && (effect.grantCumulativeUpkeepCost() == null || effect.grantCumulativeUpkeepCost().isBlank())
                && effect.grantOnDeathEffect() == null
                && (effect.battlefieldEffectGrants() == null || effect.battlefieldEffectGrants().isEmpty())
                && (effect.perpetualBattlefieldEffectGrants() == null
                || effect.perpetualBattlefieldEffectGrants().isEmpty())
                && !effect.perpetuallySetBasePowerToSourcePower()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        boolean cardOnBattlefield = battlefield != null && battlefield.stream()
                .anyMatch(p -> p.getCard().getId().equals(card.getId()));
        if (!cardOnBattlefield) {
            // The return was prevented or replaced, but "perpetually" still applies to the physical card.
            if (effect.perpetualBattlefieldEffectGrants() != null) {
                PerpetualCardBattlefieldEffectSupport.remember(
                        gameData, card, effect.perpetualBattlefieldEffectGrants());
            }
            return;
        }
        for (Permanent p : battlefield) {
            if (!p.getCard().getId().equals(card.getId())) {
                continue;
            }
            if (effect.enterWithMannequinCounter()) {
                permanentCounterSupport.placeCounterOnPermanentForPlayer(gameData, entry, p,
                        CounterType.MANNEQUIN, 1, entry == null ? controllerId : entry.getControllerId());
                p.setMannequinGrantActive(true);
            }
            if (enterWithCounter) {
                permanentCounterSupport.placeCounterOnPermanentForPlayer(gameData, entry, p,
                        effect.enterWithCounter(), effect.enterWithCounterCount(),
                        entry == null ? controllerId : entry.getControllerId());
            }
            if (enterWithCounters) {
                for (CounterType counterType : effect.enterWithCounters()) {
                    int placed = gameQueryService.replaceCounters(
                            gameData, p, controllerId, counterType, 1, entry == null ? controllerId : entry.getControllerId());
                    if (placed > 0) {
                        p.setCounterCount(counterType, p.getCounterCount(counterType) + placed);
                        p.setCounterTimestamp(counterType, gameData.nextTimestamp());
                    }
                }
            }
            if (effect.exileIfLeavesBattlefield()) {
                p.setExileIfLeavesBattlefield(true);
            }
            if (effect.exileIfDying()) {
                p.setExileIfDying(true);
                p.setExileIfDyingTimestamp(gameData.nextTimestamp());
            }
            if (effect.unearth()) {
                p.setEnteredViaUnearth(true);
            }
            if (effect.perpetuallySetBasePowerToSourcePower() && entry != null) {
                Permanent source = entry.getSourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (source == null) {
                    source = entry.getSourcePermanentSnapshot();
                }
                if (source != null) {
                    Card modifiedCard = p.getCard().createRuntimeCopy();
                    modifiedCard.setPower(gameQueryService.getEffectivePower(gameData, source));
                    p.exchangeCard(modifiedCard);
                }
            }
            if (effect.grantKeywords() != null) {
                p.getPersistentGrantedKeywords().addAll(effect.grantKeywords());
            }
            if (effect.grantHasteUntilNextTurn()) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                        entry != null ? entry.getCard().getName() : card.getName(),
                        entry != null ? entry.getSourcePermanentId() : null,
                        entry != null ? entry.getControllerId() : controllerId,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET), p.getId(),
                        null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
            }
            if (effect.grantSubtypes() != null) {
                effect.grantSubtypes().stream()
                        .filter(subtype -> !p.getGrantedSubtypes().contains(subtype))
                        .forEach(p.getGrantedSubtypes()::add);
            }
            if (plusOneCounters) {
                boolean cardTypeMatches = effect.plusOneCountersIfCardType() == null
                        || (effect.plusOneCountersIfCardType() == CardType.CREATURE
                        ? gameQueryService.isCreature(gameData, p)
                        : p.getCard().hasType(effect.plusOneCountersIfCardType()));
                boolean subtypeMatches = effect.plusOneCountersIfSubtype() == null
                        || gameQueryService.hasEffectiveSubtype(gameData, p, effect.plusOneCountersIfSubtype());
                if (cardTypeMatches && subtypeMatches) {
                    permanentCounterSupport.applyPlusOnePlusOneCounters(
                            gameData, null, p, effect.plusOneCounterCount());
                }
            }
            if (exiledCostCounter) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, p,
                        effect.counterIfExiledCostCardHasSubtype(),
                        effect.counterCountIfExiledCostCardHasSubtype());
            }
            if (effect.grantCumulativeUpkeepCost() != null && !effect.grantCumulativeUpkeepCost().isBlank()) {
                p.addPersistentTriggeredEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new CumulativeUpkeepEffect(effect.grantCumulativeUpkeepCost()));
            }
            grantOnDeathEffect(p, effect.grantOnDeathEffect(), cardOwnerId != null ? cardOwnerId : card.getOwnerId());
            if (effect.battlefieldEffectGrants() != null) {
                String sourceCardName = entry == null ? card.getName() : entry.getCard().getName();
                UUID sourcePermanentId = entry == null ? null : entry.getSourcePermanentId();
                for (CardEffect grantedEffect : effect.battlefieldEffectGrants()) {
                    if ((grantedEffect instanceof GrantColorEffect || grantedEffect instanceof SetCardTypesEffect
                            || grantedEffect instanceof GrantCardTypeEffect
                            || grantedEffect instanceof GrantSubtypeEffect
                            || grantedEffect instanceof SetBasePowerToughnessEffect)
                            && gameData.floatingEffects.stream().anyMatch(floating ->
                            grantedEffect.equals(floating.effect())
                                    && p.getId().equals(floating.affectedPermanentId())
                                    && java.util.Objects.equals(sourcePermanentId, floating.sourcePermanentId()))) {
                        continue;
                    }
                    if (grantedEffect instanceof LosesAllAbilitiesEffect loses
                            && loses.duration() == EffectDuration.PERMANENT) {
                        p.setLosesAllAbilitiesPermanently(true);
                    }
                    EffectDuration grantDuration = effect.battlefieldEffectGrantDuration() == null
                            ? EffectDuration.PERMANENT : effect.battlefieldEffectGrantDuration();
                    if (grantDuration == EffectDuration.PERMANENT
                            && grantedEffect instanceof GrantKeywordEffect keywordGrant) {
                        grantDuration = switch (keywordGrant.duration()) {
                            case END_OF_TURN -> EffectDuration.UNTIL_END_OF_TURN;
                            case UNTIL_END_OF_COMBAT -> EffectDuration.UNTIL_END_OF_COMBAT;
                            case UNTIL_YOUR_NEXT_TURN -> EffectDuration.UNTIL_YOUR_NEXT_TURN;
                            case UNTIL_YOUR_NEXT_UPKEEP -> EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP;
                            case WHILE_SOURCE_ON_BATTLEFIELD -> EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD;
                            case WHILE_SOURCE_REMAINS -> EffectDuration.WHILE_SOURCE_REMAINS;
                            case INDEFINITE -> EffectDuration.PERMANENT;
                        };
                    }
                    gameData.addFloatingEffect(new FloatingContinuousEffect(
                            UUID.randomUUID(), sourceCardName, sourcePermanentId, controllerId,
                            grantedEffect, p.getId(), null, null, grantDuration, 0));
                }
            }
            if (effect.perpetualBattlefieldEffectGrants() != null) {
                PerpetualCardBattlefieldEffectSupport.remember(
                        gameData, card, effect.perpetualBattlefieldEffectGrants());
                PerpetualCardBattlefieldEffectSupport.apply(
                        gameData, controllerId, p, effect.perpetualBattlefieldEffectGrants());
            }
            break;
        }
    }

    public void grantOnDeathEffect(Permanent permanent, CardEffect effect, UUID cardOwnerId) {
        if (permanent == null || effect == null) {
            return;
        }
        CardEffect boundEffect = effect;
        if (effect instanceof TargetPlayerLosesGameEffect losesGame && losesGame.playerId() == null
                && cardOwnerId != null) {
            boundEffect = new TargetPlayerLosesGameEffect(cardOwnerId);
        }
        permanent.addDelayedDeathEffect(boundEffect);
    }

    /**
     * Whether the returning card enters with its +1/+1 counters, so entry triggers see them. A
     * subtype-gated rider ("if a Hero enters this way, it enters with ... counters") applies on
     * entry whenever the card has that subtype, changeling included.
     */
    private boolean entersWithPlusOneCounters(GameData gameData, Card card, UUID cardOwnerId,
                                              ReturnCardFromGraveyardEffect effect) {
        if (effect.plusOneCountersIfSubtype() != null) {
            return effect.plusOneCounterCount() > 0
                    && effect.plusOneCountersIfCardType() == null
                    && effect.plusOneCountersIfCondition() == null
                    && effect.plusOneCountersIfExiledCostCardHasSubtype() == null
                    && !(effect.grantHaste() || effect.exileAtEndStep() || effect.exileAtYourNextEndStep()
                    || effect.sacrificeAtEndStep())
                    && gameQueryService.cardHasSubtype(
                    card, effect.plusOneCountersIfSubtype(), gameData, cardOwnerId);
        }
        if (effect.plusOneCountersIfCardType() != null) {
            return effect.plusOneCounterCount() > 0
                    && effect.plusOneCountersIfCondition() == null
                    && effect.plusOneCountersIfExiledCostCardHasSubtype() == null
                    && !(effect.grantHaste() || effect.exileAtEndStep() || effect.exileAtYourNextEndStep()
                    || effect.sacrificeAtEndStep())
                    && card.hasType(effect.plusOneCountersIfCardType());
        }
        return entersWithUnconditionalPlusOneCounters(effect);
    }

    /**
     * Whether a conditional {@code plusOneCountersIfCondition} is met as the card enters, so the
     * counters are placed before entry triggers check power (e.g. Necromantic Summons' spell mastery).
     */
    private boolean entersWithConditionalPlusOneCounters(GameData gameData, StackEntry entry, UUID controllerId,
                                                         ReturnCardFromGraveyardEffect effect) {
        return effect.plusOneCounterCount() > 0
                && effect.plusOneCountersIfCondition() != null
                && effect.plusOneCountersIfSubtype() == null
                && effect.plusOneCountersIfCardType() == null
                && effect.plusOneCountersIfExiledCostCardHasSubtype() == null
                && !(effect.grantHaste() || effect.exileAtEndStep() || effect.exileAtYourNextEndStep()
                || effect.sacrificeAtEndStep())
                && conditionEvaluationService.isMet(gameData, effect.plusOneCountersIfCondition(),
                entry == null
                        ? ConditionContext.forCasting(controllerId)
                        : ConditionContext.forStackEntry(entry));
    }

    private boolean entersWithUnconditionalPlusOneCounters(ReturnCardFromGraveyardEffect effect) {
        return effect.plusOneCounterCount() > 0
                && effect.plusOneCountersIfSubtype() == null
                && effect.plusOneCountersIfCardType() == null
                && effect.plusOneCountersIfCondition() == null
                && effect.plusOneCountersIfExiledCostCardHasSubtype() == null
                && !(effect.grantHaste() || effect.exileAtEndStep() || effect.exileAtYourNextEndStep()
                || effect.sacrificeAtEndStep());
    }

    /**
     * An Aura that enchants a card in a graveyard (Animate Dead) returned to the battlefield enters
     * attached to a card of its controller's choice that it could have enchanted as a spell.
     *
     * @return {@code true} if {@code auraCard} enchants a graveyard card, so the usual permanent or
     *         player attachment doesn't apply (the Aura stays in the graveyard when no card qualifies)
     */
    private boolean beginGraveyardCardAuraAttachment(GameData gameData, StackEntry entry,
                                                     ReturnCardFromGraveyardEffect effect, Card auraCard,
                                                     UUID auraOwnerId, UUID controllerId) {
        com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport.Target target = graveyardTargetingSupport.findTarget(
                auraCard.getEffects(EffectSlot.SPELL));
        if (target == null) {
            return false;
        }
        UUID auraControllerId = effect.underOwnersControl() && auraOwnerId != null ? auraOwnerId : controllerId;
        List<Card> candidates = new ArrayList<>();
        for (UUID graveyardOwnerId : target.scope().graveyardOwners(gameData.orderedPlayerIds, auraControllerId)) {
            for (Card candidate : gameData.playerGraveyards.getOrDefault(graveyardOwnerId, List.of())) {
                if (!candidate.getId().equals(auraCard.getId())
                        && (target.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        candidate, target.filter(), auraCard.getId(), gameData, graveyardOwnerId))) {
                    candidates.add(candidate);
                }
            }
        }
        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText(entry.getDescription() + " leaves ",
                    auraCard, " in the graveyard because it cannot legally enchant anything."));
            return true;
        }
        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCard.getId());
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(auraControllerId, IntStream.range(0, candidates.size()).boxed().toList(),
                        GraveyardChoiceDestination.ENCHANT_GRAVEYARD_CARD,
                        "Choose a card in a graveyard for " + auraCard.getName() + " to enchant.")
                .cardPool(candidates)
                .mandatory(true)
                .mayAbilityContext(auraCard, auraControllerId, List.of(), null)
                .build());
        return true;
    }

    /** Puts an Aura onto the battlefield attached to the chosen graveyard card; its enter ability does the rest. */
    public void putAuraOntoBattlefieldEnchantingGraveyardCard(GameData gameData, UUID controllerId, Card auraCard,
                                                              Card enchantedCard) {
        Permanent auraPermanent = new Permanent(auraCard);
        auraPermanent.setAttachedTo(enchantedCard.getId());
        auraPermanent.setEnteredFromZone(Zone.GRAVEYARD);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, auraPermanent);
        gameLogService.append(gameData, GameLog.builder().card(auraCard)
                .text(" enters the battlefield attached to ").card(enchantedCard)
                .text(" under " + gameData.playerIdToName.get(controllerId) + "'s control.").build());
        handleCreatureEtbAndLegendRule(gameData, controllerId, auraPermanent, auraCard);
    }

    private boolean losesAllAbilitiesBeforeEntering(ReturnCardFromGraveyardEffect effect) {
        return effect.battlefieldEffectGrants() != null
                && effect.battlefieldEffectGrants().stream()
                .anyMatch(LosesAllAbilitiesEffect.class::isInstance);
    }

    public void resolveReturnAll(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                  UUID controllerId, UUID sourceCardId) {
        if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD && effect.chooseAuraAttachment()) {
            List<BattlefieldEntryCard> cards = new ArrayList<>();
            for (var graveyard : gameData.playerGraveyards.entrySet()) {
                if (effect.source() != GraveyardSearchScope.ALL_GRAVEYARDS
                        && !graveyard.getKey().equals(controllerId)) continue;
                Set<UUID> battlefieldTrackedIds = effect.fromBattlefieldThisTurn()
                        ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(graveyard.getKey(), Set.of())
                        : null;
                for (Card card : graveyard.getValue()) {
                    if (battlefieldTrackedIds != null && !battlefieldTrackedIds.contains(card.getId())) {
                        continue;
                    }
                    if (matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                        UUID recipientId = effect.underOwnersControl()
                                ? card.getOwnerId() == null ? graveyard.getKey() : card.getOwnerId()
                                : controllerId;
                        cards.add(new BattlefieldEntryCard(
                                recipientId, graveyard.getKey(), card, Zone.GRAVEYARD, null)
                                .withTapped(effect.enterTapped()));
                    }
                }
            }
            battlefieldEntryBatchSupport.begin(gameData, cards);
            return;
        }
        if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD
                && effect.attachmentTarget() != null) {
            resolveReturnAllWithAttachments(gameData, entry, effect, controllerId, sourceCardId);
            return;
        }

        if (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS
                && (effect.fromAnywhereThisTurn() || effect.fromBattlefieldThisTurn())) {
            resolveTrackedReturnAllFromAllGraveyards(gameData, entry, effect, controllerId, sourceCardId);
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        

        if (effect.thisTurnOnly() || effect.fromBattlefieldThisTurn() || effect.fromAnywhereThisTurn()
                || effect.discardedOrCycledThisTurn() || effect.discardedByOpponentThisTurn()) {
            Set<UUID> trackedIds;
            String sourceLabel;
            if (effect.discardedByOpponentThisTurn()) {
                trackedIds = gameData.cardsDiscardedByOpponentThisTurn.getOrDefault(controllerId, Set.of());
                if (!entry.getEventCardIds().isEmpty()) {
                    // "Return those cards": only the cards discarded in the event that triggered this.
                    trackedIds = trackedIds.stream().filter(entry.getEventCardIds()::contains)
                            .collect(java.util.stream.Collectors.toCollection(HashSet::new));
                }
                sourceLabel = "because of an opponent's spell or ability";
            } else if (effect.discardedOrCycledThisTurn()) {
                trackedIds = gameData.cardsDiscardedOrCycledThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "by cycling or discarding";
            } else if (effect.fromAnywhereThisTurn()) {
                trackedIds = gameData.cardsPutIntoGraveyardFromAnywhereThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "from anywhere";
            } else if (effect.fromBattlefieldThisTurn()) {
                trackedIds = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "from the battlefield";
            } else {
                trackedIds = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(controllerId, Set.of());
                sourceLabel = "from the battlefield";
            }

            if (graveyard == null || graveyard.isEmpty() || trackedIds.isEmpty()) {
                String logEntry = entry.getDescription() + " - no cards were put into your graveyard " + sourceLabel + " this turn.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                return;
            }

            List<Card> toReturn = new ArrayList<>();
            for (Card card : graveyard) {
                if (!card.isToken()
                        && trackedIds.contains(card.getId())
                        && matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                    toReturn.add(card);
                }
            }

            if (toReturn.isEmpty()) {
                String logEntry = entry.getDescription() + " - no cards were put into your graveyard " + sourceLabel + " this turn.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                return;
            }

            boolean toBattlefield = effect.destination() == GraveyardChoiceDestination.BATTLEFIELD;
            List<ReturnedPermanent> deferredEtbs = new ArrayList<>();
            boolean alreadyCollectingEntries = gameData.collectingSimultaneousCreatureEntryTriggers;
            if (toBattlefield) {
                gameData.collectingSimultaneousCreatureEntryTriggers = true;
                if (!alreadyCollectingEntries) {
                    gameData.simultaneousCreatureEntryTriggers.clear();
                }
            }
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : toReturn) {
                    graveyard.remove(card);
                    trackedIds.remove(card.getId());
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
                    keepOwnAbilityBoundToMovedSource(gameData, entry, card);
                    if (toBattlefield) {
                        Permanent returnedPermanent = putCardOntoBattlefield(gameData, controllerId, card,
                                effect.grantColor(), effect.grantSubtype(), effect.enterTapped(), false,
                                null, false, losesAllAbilitiesBeforeEntering(effect), 0,
                                permanent -> { }, deferredEtbs);
                        applyBattlefieldReturnRiders(gameData, controllerId, card, effect, entry, controllerId);
                        queueNextUpkeepExile(gameData, entry, effect, controllerId, returnedPermanent);
                    } else {
                        permanentRemovalService.addCardToHandFromGraveyard(
                                gameData, controllerId, controllerId, card);
                    }
                }
                for (ReturnedPermanent returned : deferredEtbs) {
                    handleCreatureEtbAndLegendRule(gameData, returned.controllerId(), returned.permanent(),
                            returned.card());
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
                gameData.collectingSimultaneousCreatureEntryTriggers = alreadyCollectingEntries;
                if (!alreadyCollectingEntries) {
                    gameData.simultaneousCreatureEntryTriggers.clear();
                }
            }

            String destinationLabel = toBattlefield ? " from graveyard to the battlefield." : " from graveyard to hand.";
            String playerName = gameData.playerIdToName.get(controllerId);
            GameLog.Builder builder = GameLog.builder().text(playerName + " returns ");
            appendCardList(builder, toReturn);
            builder.text(destinationLabel);
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} returns {} card(s) from graveyard to {}",
                    gameData.id, playerName, toReturn.size(), toBattlefield ? "battlefield" : "hand");
            return;
        }

        // CR 400.7: a trigger that returns its own card refers to one stay in the graveyard; once the card has
        // left and re-entered it is a new object and the return does nothing.
        if (effect.filter() instanceof com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate
                && entry != null && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && entry.getCard() != null && entry.getCard().getId().equals(entry.getTriggeringCardId())
                && gameData.graveyardEntryVersion(entry.getCard().getId())
                != entry.getTriggeringCardGraveyardEntryVersion()) {
            return;
        }

        // Return all matching — collect from either controller's graveyard or all graveyards. A card returning
        // itself is searched for wherever it landed: its owner's graveyard, which isn't the controller's
        // when another player controlled the permanent it came from (Eye of Nidhogg on a stolen creature).
        Map<UUID, List<Card>> graveyardsToSearch = new LinkedHashMap<>();
        if (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS
                || effect.filter() instanceof com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate) {
            for (Map.Entry<UUID, List<Card>> gyEntry : gameData.playerGraveyards.entrySet()) {
                if (gyEntry.getValue() != null && !gyEntry.getValue().isEmpty()) {
                    graveyardsToSearch.put(gyEntry.getKey(), gyEntry.getValue());
                }
            }
        } else {
            if (graveyard != null && !graveyard.isEmpty()) {
                graveyardsToSearch.put(controllerId, graveyard);
            }
        }

        if (graveyardsToSearch.isEmpty()) {
            return;
        }

        List<Card> returnedCards = new ArrayList<>();
        List<ReturnedPermanent> deferredEtbs = new ArrayList<>();
        boolean alreadyCollectingEntries = gameData.collectingSimultaneousCreatureEntryTriggers;
        if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD) {
            gameData.collectingSimultaneousCreatureEntryTriggers = true;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Map.Entry<UUID, List<Card>> gyEntry : graveyardsToSearch.entrySet()) {
                List<Card> gy = gyEntry.getValue();
                List<Card> toReturn = new ArrayList<>();
                for (Card card : gy) {
                    if (matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                        toReturn.add(card);
                    }
                }
                for (Card card : toReturn) {
                    gy.remove(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, gyEntry.getKey(), card);
                    keepOwnAbilityBoundToMovedSource(gameData, entry, card);
                    boolean toOwnersLibrary = effect.destination() == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY
                            || effect.destination() == GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY;
                    UUID targetPlayerId = (effect.destination() == GraveyardChoiceDestination.HAND
                            || toOwnersLibrary || effect.underOwnersControl())
                            ? gyEntry.getKey() : controllerId;
                    if (effect.destination() == GraveyardChoiceDestination.HAND) {
                        permanentRemovalService.addCardToHandFromGraveyard(
                                gameData, gyEntry.getKey(), targetPlayerId, card);
                    } else if (effect.destination() == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY) {
                        gameData.playerDecks.get(targetPlayerId).addFirst(card);
                    } else if (effect.destination() == GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY) {
                        gameData.playerDecks.get(targetPlayerId).addLast(card);
                    } else {
                        Permanent returnedPermanent;
                        boolean entersWithPlusOneCounters = entersWithPlusOneCounters(
                                gameData, card, gyEntry.getKey(), effect);
                        if (effect.grantHaste() || effect.exileAtEndStep() || effect.exileAtYourNextEndStep()
                                || effect.sacrificeAtEndStep()) {
                            returnedPermanent = putCardOntoBattlefieldWithHasteAndExile(gameData, targetPlayerId, card,
                                    effect.grantHaste(), effect.exileAtEndStep(), effect.sacrificeAtEndStep(),
                                    effect.exileIfLeavesBattlefield(), effect.enterTapped(), effect.enterAttacking(),
                                    effect.exileAtYourNextEndStep(), losesAllAbilitiesBeforeEntering(effect), entry.getCard(), deferredEtbs);
                            limitHasteToThisTurn(effect, returnedPermanent);
                        } else {
                            returnedPermanent = putCardOntoBattlefield(gameData, targetPlayerId, card,
                                    effect.grantColor(), effect.grantSubtype(), effect.enterTapped(),
                                    effect.enterAttacking(),
                                    entersWithPlusOneCounters ? CounterType.PLUS_ONE_PLUS_ONE : null, false,
                                    losesAllAbilitiesBeforeEntering(effect),
                                    entersWithPlusOneCounters ? effect.plusOneCounterCount() : 0,
                                    permanent -> { }, deferredEtbs);
                        }
                        if (returnedPermanent == null && !gyEntry.getKey().equals(targetPlayerId)) {
                            List<Card> blockedIn = gameData.playerGraveyards.get(targetPlayerId);
                            if (blockedIn != null && blockedIn.remove(card)) {
                                gameData.playerGraveyards.computeIfAbsent(gyEntry.getKey(), k -> new ArrayList<>())
                                        .add(card);
                            }
                        }
                        applyBattlefieldReturnRiders(gameData, targetPlayerId, card, effect, entry,
                                gyEntry.getKey(), entersWithPlusOneCounters);
                        queueNextUpkeepExile(gameData, entry, effect, targetPlayerId, returnedPermanent);
                        if (returnedPermanent != null && effect.enterAttacking()) {
                            makeChosenPermanentAttackingEffectHandler.resolve(gameData, entry,
                                    new com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect(
                                            returnedPermanent.getId()));
                        }
                    }
                    returnedCards.add(card);
                }
            }
            deferredEtbs.forEach(returned ->
                    gameData.permanentsEnteringSimultaneously.add(returned.permanent().getId()));
            for (ReturnedPermanent returned : deferredEtbs) {
                handleCreatureEtbAndLegendRule(gameData, returned.controllerId(), returned.permanent(), returned.card());
            }
        } finally {
            deferredEtbs.forEach(returned ->
                    gameData.permanentsEnteringSimultaneously.remove(returned.permanent().getId()));
            graveyardService.endGraveyardLeaveBatch(gameData);
            gameData.collectingSimultaneousCreatureEntryTriggers = alreadyCollectingEntries;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }

        if (returnedCards.isEmpty()) {
            return;
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destName = switch (effect.destination()) {
            case HAND -> "hand";
            case TOP_OF_OWNERS_LIBRARY -> "the top of the library";
            case BOTTOM_OF_OWNERS_LIBRARY -> "the bottom of the library";
            default -> "the battlefield";
        };
        GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
        appendCardList(builder, returnedCards);
        builder.text(" onto " + destName + " from "
                + (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS ? "all graveyards" : "graveyard") + ".");
        gameLogService.append(gameData, builder.build());
        log.info("Game {} - {} puts {} onto {} from graveyards", gameData.id, playerName,
                returnedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse(""), destName);
    }

    private void resolveTrackedReturnAllFromAllGraveyards(
            GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
            UUID controllerId, UUID sourceCardId) {
        Map<UUID, List<Card>> cardsByGraveyard = new LinkedHashMap<>();
        Map<UUID, Set<UUID>> trackedCardsByGraveyard = effect.fromBattlefieldThisTurn()
                ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn
                : gameData.cardsPutIntoGraveyardFromAnywhereThisTurn;
        for (Map.Entry<UUID, List<Card>> graveyardEntry : gameData.playerGraveyards.entrySet()) {
            Set<UUID> trackedIds = trackedCardsByGraveyard.get(graveyardEntry.getKey());
            if (trackedIds == null || trackedIds.isEmpty()) {
                continue;
            }
            List<Card> matchingCards = new ArrayList<>();
            for (Card card : graveyardEntry.getValue()) {
                if (!card.isToken()
                        && trackedIds.contains(card.getId())
                        && matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                    matchingCards.add(card);
                }
            }
            if (!matchingCards.isEmpty()) {
                cardsByGraveyard.put(graveyardEntry.getKey(), matchingCards);
            }
        }

        if (cardsByGraveyard.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription()
                            + " - no cards were put into graveyards from anywhere this turn."));
            return;
        }

        List<Card> returnedCards = new ArrayList<>();
        List<ReturnedPermanent> deferredEtbs = new ArrayList<>();
        boolean alreadyCollectingEntries = gameData.collectingSimultaneousCreatureEntryTriggers;
        if (effect.destination() == GraveyardChoiceDestination.BATTLEFIELD) {
            gameData.collectingSimultaneousCreatureEntryTriggers = true;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Map.Entry<UUID, List<Card>> cardsEntry : cardsByGraveyard.entrySet()) {
                UUID graveyardOwnerId = cardsEntry.getKey();
                List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
                Set<UUID> trackedIds = trackedCardsByGraveyard.get(graveyardOwnerId);
                for (Card card : cardsEntry.getValue()) {
                    graveyard.remove(card);
                    if (trackedIds != null) {
                        trackedIds.remove(card.getId());
                    }
                    graveyardService.notifyCardsLeftGraveyard(gameData, graveyardOwnerId, card);
                    keepOwnAbilityBoundToMovedSource(gameData, entry, card);

                    boolean toBattlefield = effect.destination() == GraveyardChoiceDestination.BATTLEFIELD;
                    UUID targetPlayerId = (effect.destination() == GraveyardChoiceDestination.HAND
                            || effect.underOwnersControl()) ? graveyardOwnerId : controllerId;
                    if (toBattlefield) {
                        Permanent returnedPermanent;
                        if (effect.grantHaste() || effect.exileAtEndStep()
                                || effect.exileAtYourNextEndStep() || effect.sacrificeAtEndStep()) {
                            returnedPermanent = putCardOntoBattlefieldWithHasteAndExile(
                                    gameData, targetPlayerId, card, effect.grantHaste(), effect.exileAtEndStep(),
                                    effect.sacrificeAtEndStep(), effect.exileIfLeavesBattlefield(),
                                    effect.enterTapped(), effect.enterAttacking(), effect.exileAtYourNextEndStep(),
                                    losesAllAbilitiesBeforeEntering(effect), entry.getCard());
                            limitHasteToThisTurn(effect, returnedPermanent);
                        } else {
                            returnedPermanent = putCardOntoBattlefield(
                                    gameData, targetPlayerId, card, effect.grantColor(), effect.grantSubtype(),
                                    effect.enterTapped(), effect.enterAttacking(), null, false,
                                    losesAllAbilitiesBeforeEntering(effect), 0, permanent -> { }, deferredEtbs);
                        }
                        if (returnedPermanent == null) {
                            List<Card> blockedIn = gameData.playerGraveyards.get(targetPlayerId);
                            if (!graveyardOwnerId.equals(targetPlayerId) && blockedIn != null && blockedIn.remove(card)) {
                                graveyard.add(card);
                            }
                            if (trackedIds != null) {
                                trackedIds.add(card.getId());
                            }
                        }
                        applyBattlefieldReturnRiders(
                                gameData, targetPlayerId, card, effect, entry, graveyardOwnerId);
                        queueNextUpkeepExile(gameData, entry, effect, targetPlayerId, returnedPermanent);
                    } else {
                        permanentRemovalService.addCardToHandFromGraveyard(
                                gameData, graveyardOwnerId, targetPlayerId, card);
                    }
                    returnedCards.add(card);
                }
            }
            deferredEtbs.forEach(returned ->
                    gameData.permanentsEnteringSimultaneously.add(returned.permanent().getId()));
            for (ReturnedPermanent returned : deferredEtbs) {
                handleCreatureEtbAndLegendRule(gameData, returned.controllerId(), returned.permanent(), returned.card());
            }
        } finally {
            deferredEtbs.forEach(returned ->
                    gameData.permanentsEnteringSimultaneously.remove(returned.permanent().getId()));
            graveyardService.endGraveyardLeaveBatch(gameData);
            gameData.collectingSimultaneousCreatureEntryTriggers = alreadyCollectingEntries;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destination = effect.destination() == GraveyardChoiceDestination.BATTLEFIELD
                ? "the battlefield" : "hand";
        GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
        appendCardList(builder, returnedCards);
        builder.text(" onto " + destination + " from all graveyards.");
        gameLogService.append(gameData, builder.build());
    }

    private void queueNextUpkeepExile(GameData gameData, StackEntry entry,
                                      ReturnCardFromGraveyardEffect effect, UUID controllerId,
                                      Permanent returnedPermanent) {
        if (effect.exileAtNextUpkeep() && returnedPermanent != null) {
            gameData.queueDelayedAction(new ExilePermanentAtNextUpkeep(
                    controllerId, returnedPermanent.getId(), entry.getCard()));
        }
    }

    private void resolveReturnAllWithAttachments(GameData gameData, StackEntry entry,
                                                  ReturnCardFromGraveyardEffect effect,
                                                  UUID controllerId, UUID sourceCardId) {
        Map<UUID, List<Card>> graveyardsToSearch = new LinkedHashMap<>();
        if (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS) {
            for (Map.Entry<UUID, List<Card>> gyEntry : gameData.playerGraveyards.entrySet()) {
                if (gyEntry.getValue() != null && !gyEntry.getValue().isEmpty()) {
                    graveyardsToSearch.put(gyEntry.getKey(), gyEntry.getValue());
                }
            }
        } else {
            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            if (graveyard != null && !graveyard.isEmpty()) {
                graveyardsToSearch.put(controllerId, graveyard);
            }
        }

        if (graveyardsToSearch.isEmpty()) {
            return;
        }

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Map.Entry<UUID, List<Card>> gyEntry : graveyardsToSearch.entrySet()) {
                UUID graveyardOwnerId = gyEntry.getKey();
                List<Card> graveyard = gyEntry.getValue();
                for (Card card : new ArrayList<>(graveyard)) {
                    if (!matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                        continue;
                    }

                    UUID battlefieldControllerId = effect.underOwnersControl()
                            ? graveyardOwnerId : controllerId;
                    List<UUID> attachmentTargetIds = findMassAuraAttachmentTargets(
                            gameData, card, battlefieldControllerId, effect.attachmentTarget());
                    if (attachmentTargetIds.isEmpty()
                            || isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                        continue;
                    }

                    graveyard.remove(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, graveyardOwnerId, card);

                    if (attachmentTargetIds.size() == 1) {
                        gameData.retetherOperation.pendingPlacements.add(new RetetherAuraPlacement(
                                battlefieldControllerId, graveyardOwnerId, card, attachmentTargetIds.getFirst()));
                    } else {
                        gameData.retetherOperation.pendingAuraChoices.add(new RetetherAuraChoiceRequest(
                                battlefieldControllerId, graveyardOwnerId, card, attachmentTargetIds));
                    }
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!gameData.retetherOperation.pendingAuraChoices.isEmpty()) {
            beginNextRetetherAuraChoice(gameData);
        } else {
            placePendingRetetherAuras(gameData, controllerId);
        }
    }

    /** Collects attachments before a batch enters, then places the prepared permanents together. */
    public void returnPreparedPermanentsWithAuraChoices(GameData gameData, UUID controllerId,
                                                       List<Permanent> preparedPermanents,
                                                       EnterWithCountersEffect additionalCounters) {
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Permanent prepared : preparedPermanents) {
                Card card = prepared.getCard();
                UUID graveyardOwnerId = prepared.getEnteredFromGraveyardOwnerId();
                List<UUID> attachments = new ArrayList<>();
                if (card.isAura()) {
                    gameData.forEachPermanent((ignored, candidate) -> {
                        if (auraAttachmentService.canEnchant(gameData, card, controllerId, candidate)) {
                            attachments.add(candidate.getId());
                        }
                    });
                    if (card.isEnchantPlayer()) {
                        for (UUID playerId : gameData.orderedPlayerIds) {
                            if (auraAttachmentService.canEnchantPlayer(gameData, card, controllerId, playerId)) {
                                attachments.add(playerId);
                            }
                        }
                    }
                    if (attachments.isEmpty()) continue;
                }
                if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) continue;
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                if (card.isAura()) {
                    gameData.retetherOperation.pendingAuraChoices.add(new RetetherAuraChoiceRequest(
                            controllerId, graveyardOwnerId, card, attachments, prepared, additionalCounters));
                } else {
                    gameData.retetherOperation.pendingPlacements.add(new RetetherAuraPlacement(
                            controllerId, graveyardOwnerId, card, null, prepared, additionalCounters));
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        if (!gameData.retetherOperation.pendingAuraChoices.isEmpty()) {
            beginNextRetetherAuraChoice(gameData);
        } else {
            placePendingRetetherAuras(gameData, controllerId);
        }
    }

    private List<UUID> findMassAuraAttachmentTargets(GameData gameData, Card auraCard, UUID auraControllerId,
                                                      PermanentPredicate attachmentTarget) {
        if (!auraCard.isAura()) {
            return List.of();
        }

        List<UUID> result = new ArrayList<>();
        if (auraCard.isEnchantPlayer()) {
            // An unrestricted attachment target (any permanent) also admits players for an Aura that enchants
            // a player; a permanent predicate never matches a player, so no other restriction can admit one.
            if (attachmentTarget instanceof PermanentTruePredicate) {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (auraAttachmentService.canEnchantPlayer(gameData, auraCard, auraControllerId, playerId)) {
                        result.add(playerId);
                    }
                }
            }
            return result;
        }

        gameData.forEachPermanent((ignored, candidate) -> {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, candidate, attachmentTarget)
                    || !canEnchant(gameData, auraCard, auraControllerId, candidate)) {
                return;
            }

            Permanent auraSnapshot = new Permanent(auraCard);
            if (gameQueryService.hasProtectionFromSource(gameData, candidate, auraSnapshot)
                    || gameQueryService.cantBeEnchantedByOtherAuras(gameData, candidate)) {
                return;
            }
            result.add(candidate.getId());
        });
        return result;
    }

    private void beginNextRetetherAuraChoice(GameData gameData) {
        RetetherAuraChoiceRequest request = gameData.retetherOperation.pendingAuraChoices.pollFirst();
        if (request == null) {
            placePendingRetetherAuras(gameData, null);
            return;
        }

        gameData.retetherOperation.activeChoice = request;
        gameData.interaction.setPendingAuraCard(request.auraCard());
        List<UUID> playerTargets = request.validTargetIds().stream().filter(gameData.playerIds::contains).toList();
        List<UUID> permanentTargets = request.validTargetIds().stream()
                .filter(id -> !gameData.playerIds.contains(id)).toList();
        if (playerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, request.controllerId(), permanentTargets,
                    "Choose a permanent for " + request.auraCard().getName() + " to enchant.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, request.controllerId(), permanentTargets, playerTargets,
                    "Choose a permanent or player for " + request.auraCard().getName() + " to enchant.");
        }
    }

    public boolean completeRetetherAuraChoice(GameData gameData, UUID playerId, UUID permanentId) {
        RetetherAuraChoiceRequest request = gameData.retetherOperation.activeChoice;
        if (request == null || !request.controllerId().equals(playerId)
                || !request.validTargetIds().contains(permanentId)) {
            throw new IllegalStateException("Invalid Retether Aura choice");
        }
        if (gameQueryService.findPermanentById(gameData, permanentId) == null
                && !gameData.playerIds.contains(permanentId)) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        Card auraCard = gameData.interaction.consumePendingAuraCard();
        if (auraCard == null) {
            throw new IllegalStateException("No pending Retether Aura");
        }
        gameData.retetherOperation.pendingPlacements.add(new RetetherAuraPlacement(
                request.controllerId(), request.graveyardOwnerId(), auraCard, permanentId,
                request.preparedPermanent(), request.additionalCounters()));
        gameData.retetherOperation.activeChoice = null;

        if (!gameData.retetherOperation.pendingAuraChoices.isEmpty()) {
            beginNextRetetherAuraChoice(gameData);
            return false;
        }

        placePendingRetetherAuras(gameData, playerId);
        return true;
    }

    private void placePendingRetetherAuras(GameData gameData, UUID logControllerId) {
        if (gameData.retetherOperation.pendingPlacements.isEmpty()) {
            gameData.retetherOperation.activeChoice = null;
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<ReturnedPermanent> enteredPermanents = new ArrayList<>();
        List<Card> enteredCards = new ArrayList<>();

        for (RetetherAuraPlacement placement : gameData.retetherOperation.pendingPlacements) {
            Permanent attachmentTarget = gameQueryService.findPermanentById(gameData, placement.attachmentTargetId());
            boolean playerAttachment = placement.attachmentTargetId() != null
                    && gameData.playerIds.contains(placement.attachmentTargetId());
            if ((placement.auraCard().isAura() && attachmentTarget == null && !playerAttachment)
                    || isCardBlockedFromEnteringFromZone(gameData, placement.auraCard(), Zone.GRAVEYARD)) {
                gameData.playerGraveyards.computeIfAbsent(placement.graveyardOwnerId(), ignored -> new ArrayList<>())
                        .add(placement.auraCard());
                continue;
            }

            Permanent permanent = placement.preparedPermanent() != null
                    ? placement.preparedPermanent() : new Permanent(placement.auraCard());
            permanent.setAttachedTo(placement.attachmentTargetId());
            permanent.setEnteredFromGraveyardOwnerId(placement.graveyardOwnerId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, placement.controllerId(), permanent,
                    enterTappedTypes, simultaneouslyEntered, placement.additionalCounters());
            simultaneouslyEntered.add(permanent);
            enteredPermanents.add(new ReturnedPermanent(placement.controllerId(), permanent, placement.auraCard()));
            enteredCards.add(placement.auraCard());
        }

        for (ReturnedPermanent returned : enteredPermanents) {
            handleCreatureEtbAndLegendRule(gameData, returned.controllerId(), returned.permanent(), returned.card());
        }

        if (!enteredCards.isEmpty()) {
            UUID controllerId = logControllerId != null ? logControllerId :
                    gameData.retetherOperation.pendingPlacements.getFirst().controllerId();
            String playerName = gameData.playerIdToName.get(controllerId);
            GameLog.Builder builder = GameLog.builder().text(playerName + " puts ");
            appendCardList(builder, enteredCards);
            builder.text(" onto the battlefield from the graveyard.");
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} returns {} Aura card(s) from graveyard attached to creatures",
                    gameData.id, playerName, enteredCards.size());
        }

        gameData.retetherOperation.pendingPlacements.clear();
        gameData.retetherOperation.activeChoice = null;
    }

    public void resolveReturnAtRandom(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                        UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        // Exile the source card from graveyard before selecting random cards (e.g. Moldgraf Monstrosity)
        exileSourceFromGraveyardIfRequested(gameData, entry, effect, controllerId, sourceCardId);
        if (effect.exileSourceFromGraveyard() && graveyard != null && sourceCardId != null) {
            Card sourceCard = graveyard.stream()
                    .filter(c -> c.getId().equals(sourceCardId))
                    .findFirst()
                    .orElse(null);
            if (sourceCard != null) {
                graveyard.remove(sourceCard);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, controllerId, sourceCard);
                exileService.exileCard(gameData, controllerId, sourceCard);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameLogService.append(gameData, GameLog.textCardText(playerName + " exiles " , sourceCard, " from graveyard."));
                log.info("Game {} - {} exiles {} from graveyard", gameData.id, playerName, sourceCard.getName());
            }
        }

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        if (effect.shuffleGraveyardBeforeRandomSelection()) {
            Collections.shuffle(graveyard, ThreadLocalRandom.current());
        }

        List<Card> matchingCards = new ArrayList<>();
        for (Card card : graveyard) {
            if (matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                matchingCards.add(card);
            }
        }

        if (matchingCards.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        int requestedCount = effect.randomCountAmount() == null
                ? effect.randomCount()
                : amountEvaluationService.evaluate(gameData, effect.randomCountAmount(),
                        com.github.laxika.magicalvibes.service.effect.AmountContext.forStackEntry(entry, null));
        int count = Math.min(Math.max(requestedCount, 0), matchingCards.size());
        List<Card> returnedCards = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (int i = 0; i < count; i++) {
                Card randomCard = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
                matchingCards.remove(randomCard);
                graveyard.remove(randomCard);

                boolean conditionalDestination = effect.battlefieldIfCreatureElseHand()
                        || effect.battlefieldIfCreatureElseExile();
                boolean toBattlefield = conditionalDestination
                        ? randomCard.hasType(CardType.CREATURE)
                        : effect.destination() != GraveyardChoiceDestination.HAND;
                if (!toBattlefield && effect.battlefieldIfCreatureElseExile()) {
                    graveyardService.notifyCardsExiledFromGraveyard(gameData, controllerId, randomCard);
                } else {
                    graveyardService.notifyCardLeftGraveyard(gameData, controllerId, randomCard);
                }
                if (toBattlefield) {
                    if (effect.grantHaste() || effect.exileAtEndStep() || effect.exileAtYourNextEndStep()
                            || effect.sacrificeAtEndStep()) {
                        putCardOntoBattlefieldWithHasteAndExile(gameData, controllerId, randomCard,
                                effect.grantHaste(), effect.exileAtEndStep(), effect.sacrificeAtEndStep(),
                                effect.exileIfLeavesBattlefield(), effect.enterTapped(), effect.enterAttacking(),
                                effect.exileAtYourNextEndStep(), losesAllAbilitiesBeforeEntering(effect), entry.getCard());
                    } else {
                        putCardOntoBattlefield(gameData, controllerId, randomCard,
                                effect.grantColor(), effect.grantSubtype(), effect.enterTapped(), effect.enterAttacking(),
                                null, false, losesAllAbilitiesBeforeEntering(effect));
                    }
                    applyBattlefieldReturnRiders(gameData, controllerId, randomCard, effect, null, controllerId);
                } else if (effect.battlefieldIfCreatureElseExile()) {
                    exileService.exileCard(gameData, controllerId, randomCard);
                } else {
                    permanentRemovalService.addCardToHandFromGraveyard(
                            gameData, controllerId, controllerId, randomCard);
                }
                returnedCards.add(randomCard);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destText = effect.battlefieldIfCreatureElseHand()
                ? "hand or the battlefield"
                : effect.battlefieldIfCreatureElseExile()
                ? "exile or the battlefield"
                : effect.destination() == GraveyardChoiceDestination.HAND ? "hand" : "the battlefield";
        GameLog.Builder builder = GameLog.builder().text(playerName + " returns ");
        appendCardList(builder, returnedCards);
        builder.text(" at random from graveyard to " + destText + ".");
        gameLogService.append(gameData, builder.build());
        log.info("Game {} - {} returns {} at random from graveyard to {}",
                gameData.id, playerName, returnedCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse(""), destText);
    }

    /**
     * Returns the topmost matching card of the controller's ordered graveyard (the most recently put
     * there), with no decision for the controller — Shallow Grave's "the top creature card of your
     * graveyard". Nothing happens when the graveyard holds no matching card.
     */
    public void resolveTopmostFromControllersGraveyard(GameData gameData, StackEntry entry,
                                                       ReturnCardFromGraveyardEffect effect,
                                                       UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (int i = graveyard.size() - 1; i >= 0; i--) {
                Card card = graveyard.get(i);
                if (matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                    resolvePreTargetedById(gameData, entry, effect, controllerId, sourceCardId, card.getId());
                    return;
                }
            }
        }
        gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no "
                + CardPredicateUtils.describeFilter(effect.filter()) + "s in graveyard."));
    }

    public void resolveFromControllersGraveyard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                                 UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (matchesReturnCardFilter(gameData, entry, effect, graveyard.get(i), sourceCardId)) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        // Greatest-power restriction (e.g. Desecrator Hag): keep only the matching card(s) tied for
        // the greatest power. A single such card is a forced return; ties let the controller choose.
        if (effect.greatestPower()) {
            int maxPower = matchingIndices.stream()
                    .map(i -> gameQueryService.getEffectiveCardPower(gameData, graveyard.get(i)))
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(Integer.MIN_VALUE);
            matchingIndices = matchingIndices.stream()
                    .filter(i -> {
                        Integer power = gameQueryService.getEffectiveCardPower(gameData, graveyard.get(i));
                        return power != null && power == maxPower;
                    })
                    .toList();
        }

        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield";
        String prompt = "Return a " + filterLabel + " from your graveyard to " + destText + ".";

        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, effect.destination(), prompt)
                .enterTapped(effect.enterTapped())
                .mandatory(effect.mandatory() || effect.greatestPower())
                .enterWithCounter(effect.enterWithCounter(), effect.enterWithCounterCount())
                .enterWithCounters(effect.enterWithCounters())
                .mandatory(effect.mandatory() || effect.greatestPower())
                .gainLifeEqualToManaValue(effect.gainLifeEqualToManaValue());
        if (effect.plusOneCounterCount() > 0
                && effect.plusOneCountersIfSubtype() == null
                && effect.plusOneCountersIfCardType() == null
                && effect.plusOneCountersIfCondition() == null) {
            choice.enterWithCounter(CounterType.PLUS_ONE_PLUS_ONE, effect.plusOneCounterCount());
        }
        if (effect.grantColor() != null) {
            choice.grantColor(effect.grantColor());
        }
        if (effect.grantSubtype() != null) {
            choice.grantSubtype(effect.grantSubtype());
        }
        if (effect.grantSourceHasteIfSubtype() != null) {
            choice.grantSourceHasteIfSubtype(effect.grantSourceHasteIfSubtype(), entry.getSourcePermanentId());
        }
        if (effect.grantOnDeathEffect() != null) {
            choice.grantOnDeathEffect(effect.grantOnDeathEffect());
        }

        if (effect.attachToSource() && entry.getSourcePermanentId() != null
                && gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) != null) {
            choice.attachToSourcePermanentId(entry.getSourcePermanentId());
        }

        interactionHandlerRegistry.begin(gameData, choice.build());
    }

    public void resolveFromAllGraveyards(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                          UUID controllerId, UUID sourceCardId) {
        List<Card> cardPool = new ArrayList<>();
        Set<UUID> eventCardIds = effect.eventCardIdsOnly()
                ? new HashSet<>(entry.getEventCardIds())
                : Set.of();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if ((!effect.eventCardIdsOnly() || (eventCardIds.contains(card.getId())
                        && entry.getEventCardGraveyardEntryVersions().getOrDefault(card.getId(),
                                gameData.graveyardEntryVersion(card.getId()))
                        == gameData.graveyardEntryVersion(card.getId())))
                        && matchesReturnCardFilter(gameData, entry, effect, card, sourceCardId)) {
                    cardPool.add(card);
                }
            }
        }

        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (cardPool.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in any graveyard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            entry.getEffectsToResolve().removeIf(e -> e instanceof ShuffleIntoLibraryEffect);
            return;
        }

        List<Integer> indices = IntStream.range(0, cardPool.size()).boxed().toList();

        String prompt = switch (effect.destination()) {
            case HAND -> "Choose a " + filterLabel + " from a graveyard to put into your hand.";
            case TOP_OF_OWNERS_LIBRARY ->
                    "Choose a " + filterLabel + " from a graveyard to put on top of its owner's library.";
            case BOTTOM_OF_OWNERS_LIBRARY ->
                    "Choose a " + filterLabel + " from a graveyard to put on the bottom of its owner's library.";
            default -> "Choose a " + filterLabel + " from a graveyard to put onto the battlefield under your control.";
        };

        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(controllerId, indices, effect.destination(), prompt)
                .returnEffect(effect)
                .enterTapped(effect.enterTapped())
                .mandatory(effect.mandatory())
                .enterWithCounter(effect.enterWithCounter(), effect.enterWithCounterCount())
                .enterWithCounters(effect.enterWithCounters())
                .mandatory(effect.mandatory() || effect.greatestPower())
                .cardPool(cardPool);
        if (effect.grantColor() != null) {
            choice.grantColor(effect.grantColor());
        }
        if (effect.grantSubtype() != null) {
            choice.grantSubtype(effect.grantSubtype());
        }
        if (effect.grantOnDeathEffect() != null) {
            choice.grantOnDeathEffect(effect.grantOnDeathEffect());
        }
        interactionHandlerRegistry.begin(gameData, choice.build());
    }

    /**
     * Resolves a {@link PutTargetCardsFromGraveyardOnTopOfLibraryEffect} by moving each pre-targeted
     * card from the controller's graveyard to the top of their library. Cards are placed in target
     * order, so the last processed card ends up on top.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the top-of-library effect configuration
     */

    /**
     * Resolves a {@link ReturnTargetCardsFromGraveyardToHandEffect} by returning each pre-targeted
     * card from the controller's graveyard to their hand. Silently skips cards that are no longer
     * in the graveyard at resolution time.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the return-to-hand effect configuration
     */

    public int processTargetedGraveyardCards(GameData gameData, StackEntry entry,
                                               BiConsumer<List<Card>, Card> cardConsumer,
                                               String logVerbPhrase, String logSuffix) {
        return processTargetedGraveyardTargets(gameData, entry, entry.getTargetCardIds(), cardConsumer,
                logVerbPhrase, logSuffix);
    }

    public int processTargetedGraveyardCards(GameData gameData, StackEntry entry, List<UUID> targetCardIds,
                                               BiConsumer<List<Card>, Card> cardConsumer,
                                               String logVerbPhrase, String logSuffix) {
        return processTargetedGraveyardTargets(gameData, entry, targetCardIds, cardConsumer,
                logVerbPhrase, logSuffix);
    }

    public void putTargetedGraveyardCardsOnTopInChosenOrder(GameData gameData, StackEntry entry) {
        List<Card> movedCards = new ArrayList<>();
        processTargetedGraveyardTargets(gameData, entry, entry.getTargetCardIds(),
                (graveyard, card) -> movedCards.add(card),
                " puts ", " on top of their library from graveyard.");
        if (movedCards.isEmpty()) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        if (movedCards.size() == 1) {
            gameData.playerDecks.get(controllerId).addFirst(movedCards.getFirst());
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, movedCards, false, controllerId,
                "Put these cards on top of your library in any order."));
    }

    public int processTargetedGraveyardTargets(GameData gameData, StackEntry entry, List<UUID> targetIds,
                                                BiConsumer<List<Card>, Card> cardConsumer,
                                                String logVerbPhrase, String logSuffix) {
        UUID controllerId = entry.getControllerId();

        if (targetIds == null || targetIds.isEmpty()) {
            return 0;
        }

        List<Card> movedCards = new ArrayList<>();

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : targetIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && !entry.isTargetCardSameGraveyardObject(
                        cardId, gameData.graveyardEntryVersion(cardId))) {
                    // The card left the graveyard and came back: a new object, no longer the target.
                    continue;
                }
                UUID graveyardOwnerId = card == null
                        ? null
                        : gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (card != null && graveyardOwnerId == null) {
                    graveyardOwnerId = gameData.playerGraveyards.entrySet().stream()
                            .filter(e -> e.getValue() != null && e.getValue().stream()
                                    .anyMatch(c -> c.getId().equals(cardId)))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);
                }
                List<Card> graveyard = graveyardOwnerId == null
                        ? null
                        : gameData.playerGraveyards.get(graveyardOwnerId);
                if (card != null && graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                    cardConsumer.accept(graveyard, card);
                    movedCards.add(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, graveyardOwnerId, card);
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!movedCards.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            GameLog.Builder builder = GameLog.builder().text(playerName + logVerbPhrase);
            appendCardList(builder, movedCards);
            builder.text(logSuffix);
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} moved {} card(s) from graveyard", gameData.id, playerName, movedCards.size());
        }
        return movedCards.size();
    }

    public void moveCardToDestination(GameData gameData, UUID playerId, Card card,
                                       GraveyardChoiceDestination destination,
                                       CardColor grantColor, CardSubtype grantSubtype,
                                       boolean enterTapped) {
        String playerName = gameData.playerIdToName.get(playerId);
        if (destination == GraveyardChoiceDestination.HAND) {
            permanentRemovalService.addCardToHandFromGraveyard(gameData, playerId, playerId, card);
            
            gameLogService.append(gameData, GameLog.textCardText(playerName + " returns " , card, " from graveyard to hand."));
        } else if (destination == GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY
                || destination == GraveyardChoiceDestination.TOP_OF_CONTROLLERS_LIBRARY) {
            gameData.playerDecks.get(playerId).addFirst(card);
            triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, playerId, 1);
            
            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts " , card, " on top of their library from a graveyard."));
        } else if (destination == GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY) {
            gameData.playerDecks.get(playerId).addLast(card);
            triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, playerId, 1);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts " , card, " on the bottom of their library from a graveyard."));
        } else {
            putCardOntoBattlefield(gameData, playerId, card, grantColor, grantSubtype, enterTapped);
        }
    }

    public void addCardToHandFromGraveyard(GameData gameData, UUID graveyardOwnerId,
                                           UUID handOwnerId, Card card) {
        permanentRemovalService.addCardToHandFromGraveyard(gameData, graveyardOwnerId, handOwnerId, card);
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card) {
        putCardOntoBattlefield(gameData, controllerId, card, null, null);
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype) {
        putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype, false);
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype, boolean enterTapped) {
        putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype, enterTapped, false);
    }

    public void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking) {
        putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, null);
    }

    /**
     * Puts a card from a graveyard onto the battlefield, optionally with one counter of
     * {@code enterWithCounter} on it as it enters (e.g. Pyrrhic Revival's -1/-1 counter).
     */
    public Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter,
                enterWithCounter == null ? 0 : 1, false, false);
    }

    /** Puts a searched card onto the battlefield with a counted as-enters counter and optional haste. */
    public Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, int enterWithCounterCount,
                                         boolean grantHaste) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter, enterWithCounterCount,
                grantHaste, false);
    }

    /**
     * Puts a card from a graveyard onto the battlefield with the durable grants required by a
     * targeted reanimation effect.
     */
    public Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, boolean grantIndestructible) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter, grantIndestructible, false);
    }

    public Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, boolean grantIndestructible,
                                         boolean losesAllAbilities) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter, grantIndestructible,
                losesAllAbilities, enterWithCounter == null ? 0 : 1);
    }

    public Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, boolean grantIndestructible,
                                         boolean losesAllAbilities, int enterWithCounterAmount) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter, grantIndestructible,
                losesAllAbilities, enterWithCounterAmount, permanent -> { }, null);
    }

    private Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, int enterWithCounterCount,
                                         boolean grantHaste, boolean grantIndestructible) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter, grantIndestructible, false,
                enterWithCounterCount, permanent -> {
                    if (grantHaste) {
                        permanent.getGrantedKeywords().add(Keyword.HASTE);
                    }
                }, null);
    }

    private Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, boolean grantIndestructible,
                                         boolean losesAllAbilities, int enterWithCounterAmount,
                                         Consumer<Permanent> beforeEntry) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter, grantIndestructible,
                losesAllAbilities, enterWithCounterAmount, beforeEntry, null);
    }

    private Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, boolean grantIndestructible,
                                         boolean losesAllAbilities, int enterWithCounterAmount,
                                         Consumer<Permanent> beforeEntry, List<ReturnedPermanent> deferredEtbs) {
        return putCardOntoBattlefield(gameData, controllerId, card, grantColor, grantSubtype,
                enterTapped, enterAttacking, enterWithCounter, grantIndestructible, losesAllAbilities,
                enterWithCounterAmount, beforeEntry, deferredEtbs, Zone.GRAVEYARD);
    }

    /**
     * Puts a card found by a search of the graveyard, library, or hand onto the battlefield. A
     * "can't enter from [zone]" restriction is checked against the zone the card actually came from,
     * and a blocked card returns to that zone.
     */
    public Permanent putSearchedCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                                    Zone originZone, CounterType enterWithCounter,
                                                    int enterWithCounterAmount) {
        return putCardOntoBattlefield(gameData, controllerId, card, null, null, false, false,
                enterWithCounter, false, false, enterWithCounterAmount, permanent -> { }, null, originZone);
    }

    private Permanent putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card,
                                         CardColor grantColor, CardSubtype grantSubtype,
                                         boolean enterTapped, boolean enterAttacking,
                                         CounterType enterWithCounter, boolean grantIndestructible,
                                         boolean losesAllAbilities, int enterWithCounterAmount,
                                         Consumer<Permanent> beforeEntry, List<ReturnedPermanent> deferredEtbs,
                                         Zone originZone) {
        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield.
        // The card stays in the zone it was being returned from (the caller already removed it).
        if (isCardBlockedFromEnteringFromZone(gameData, card, originZone)) {
            UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : controllerId;
            String zoneText;
            switch (originZone) {
                case LIBRARY -> {
                    gameData.playerDecks.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(card);
                    zoneText = "library";
                }
                case HAND -> {
                    gameData.addCardToHand(ownerId, card);
                    zoneText = "hand";
                }
                default -> {
                    gameData.playerGraveyards.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(card);
                    zoneText = "graveyard";
                }
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " can't put ", card,
                    " onto the battlefield from a " + zoneText + "; it stays in the " + zoneText + "."));
            log.info("Game {} - {} blocked from entering the battlefield from a {}", gameData.id,
                    card.getName(), zoneText);
            return null;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        applyPermanentGrants(permanent, grantColor, grantSubtype, grantIndestructible);
        permanent.setLosesAllAbilitiesPermanently(losesAllAbilities);
        if (enterWithCounter != null && enterWithCounterAmount > 0
                && !gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) {
            int counters = gameQueryService.replaceCounters(gameData, permanent, controllerId,
                    enterWithCounter, enterWithCounterAmount, controllerId);
            permanent.setCounterCount(enterWithCounter, counters);
            if (counters > 0) {
                permanent.setCounterTimestamp(enterWithCounter, gameData.nextTimestamp());
            }
        }
        if (enterTapped) {
            permanent.tap();
        }
        permanent.setEnteredFromZone(originZone);
        if (originZone == Zone.GRAVEYARD) {
            permanent.setEnteredFromGraveyardOwnerId(controllerId);
        }
        beforeEntry.accept(permanent);
        if (!losesAllAbilities) {
            var typeEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .filter(com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect.class::isInstance)
                    .map(com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect.class::cast)
                    .findFirst().orElse(null);
            if (typeEffect != null) {
                if (typeEffect.lookAtOpponentHand()) {
                    cardRevealService.lookAtOpponentHand(gameData, controllerId);
                }
                if (enterAttacking) permanent.enterAttacking(true);
                playerInputService.beginCardTypeOnEnterChoice(
                        gameData, controllerId, card, typeEffect.excludedTypes(), permanent);
                return permanent;
            }
            var nameEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .filter(com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect.class::isInstance)
                    .map(com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect.class::cast)
                    .findFirst().orElse(null);
            if (nameEffect != null) {
                switch (nameEffect.handAccess()) {
                    case LOOK_AT_OPPONENT_HAND -> cardRevealService.lookAtOpponentHand(gameData, controllerId);
                    case REVEAL_OPPONENT_HAND -> gameData.playerIds.stream()
                            .filter(playerId -> !playerId.equals(controllerId))
                            .forEach(playerId -> cardRevealService.revealHandToAllPlayers(gameData, playerId));
                    case NONE -> { }
                }
                if (playerInputService.beginCardNameChoice(gameData, controllerId, card,
                        nameEffect.excludedTypes(), nameEffect.handAccess()
                        == com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect.HandAccess.REVEAL_OPPONENT_HAND,
                        nameEffect.nonbasicLandOnly(), permanent.getAttachedTo(), nameEffect.requiredType(), null,
                        permanent)) {
                    if (enterAttacking) permanent.enterAttacking(true);
                    return permanent;
                }
            }
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes,
                enteredBatchMates(deferredEtbs));
        if (enterAttacking) {
            permanent.enterAttacking(true);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String stateText = (enterTapped && enterAttacking) ? " tapped and attacking"
                : enterTapped ? " tapped"
                : enterAttacking ? " attacking"
                : "";
        gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", card,
                " onto the battlefield" + stateText + " from a "
                + (originZone == Zone.LIBRARY ? "library" : originZone == Zone.HAND ? "hand" : "graveyard") + "."));

        if (deferredEtbs != null) {
            deferredEtbs.add(new ReturnedPermanent(controllerId, permanent, card));
        } else {
            handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
        }
        return permanent;
    }

    /**
     * Puts all cards in {@code cardsByController} onto the battlefield as one simultaneous entry
     * batch. The map key is the owner/controller of each graveyard card. Cards blocked from
     * entering remain in their graveyards.
     */
    public void putCardsOntoBattlefieldSimultaneously(GameData gameData,
                                                       Map<UUID, List<Card>> cardsByController,
                                                       boolean enterTapped,
                                                       CounterType enterWithCounter) {
        putCardsOntoBattlefieldSimultaneously(gameData, cardsByController, enterTapped, enterWithCounter, null);
    }

    /** As above; {@code beforeEntry} runs on each new permanent before it enters the battlefield. */
    public void putCardsOntoBattlefieldSimultaneously(GameData gameData,
                                                       Map<UUID, List<Card>> cardsByController,
                                                       boolean enterTapped,
                                                       CounterType enterWithCounter,
                                                       java.util.function.Consumer<Permanent> beforeEntry) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<ReturnedPermanent> enteredPermanents = new ArrayList<>();

        for (Map.Entry<UUID, List<Card>> controllerEntry : cardsByController.entrySet()) {
            UUID controllerId = controllerEntry.getKey();
            for (Card card : controllerEntry.getValue()) {
                if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                    gameData.playerGraveyards.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(card);
                    gameLogService.append(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(controllerId) + " can't put ", card,
                            " onto the battlefield from a graveyard; it stays in the graveyard."));
                    continue;
                }

                Permanent permanent = new Permanent(card);
                if (enterWithCounter != null) {
                    permanent.setCounterCount(enterWithCounter, 1);
                }
                if (enterTapped) {
                    permanent.tap();
                }
                permanent.setEnteredFromGraveyardOwnerId(controllerId);
                if (beforeEntry != null) {
                    beforeEntry.accept(permanent);
                }
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                enteredPermanents.add(new ReturnedPermanent(controllerId, permanent, card));
            }
        }

        for (ReturnedPermanent returned : enteredPermanents) {
            handleCreatureEtbAndLegendRule(gameData, returned.controllerId(), returned.permanent(), returned.card());
        }
    }

    public void putCardsOntoBattlefieldSimultaneouslyUnderController(
            GameData gameData, PendingGraveyardReturnBatch batch) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<ReturnedPermanent> enteredPermanents = new ArrayList<>();
        boolean needsAuraChoice = false;

        List<RetetherAuraPlacement> placements = new ArrayList<>();
        for (Card card : batch.cards()) {
            UUID graveyardOwnerId = batch.graveyardOwnerByCardId().get(card.getId());
            if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                gameData.playerGraveyards.computeIfAbsent(graveyardOwnerId, ignored -> new ArrayList<>()).add(card);
                continue;
            }

            Permanent permanent = new Permanent(card);
                applyPermanentGrants(permanent, batch.grantColor(), batch.grantSubtype());
            permanent.getPersistentGrantedKeywords().addAll(batch.grantKeywords());
            permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
            if (batch.enterTapped()) {
                permanent.tap();
            }
            UUID battlefieldControllerId = batch.underOwnersControl()
                    ? card.getOwnerId() != null ? card.getOwnerId() : graveyardOwnerId
                    : batch.eachPlayerChooses() ? graveyardOwnerId : batch.controllerId();

            if (card.isAura()) {
                List<UUID> attachments = findAuraAttachmentCandidates(gameData, card, battlefieldControllerId);
                if (attachments.isEmpty()) {
                    gameData.playerGraveyards.computeIfAbsent(graveyardOwnerId, ignored -> new ArrayList<>())
                            .add(card);
                    continue;
                }
                needsAuraChoice = true;
                gameData.retetherOperation.pendingAuraChoices.add(new RetetherAuraChoiceRequest(
                        battlefieldControllerId, graveyardOwnerId, card, attachments, permanent, null));
                continue;
            }
            placements.add(new RetetherAuraPlacement(
                    battlefieldControllerId, graveyardOwnerId, card, null, permanent, null));
        }

        if (needsAuraChoice) {
            // CR 303.4f: an Aura entering without being cast is attached as the entering player chooses.
            gameData.retetherOperation.pendingPlacements.addAll(placements);
            beginNextRetetherAuraChoice(gameData);
            return;
        }

        for (RetetherAuraPlacement placement : placements) {
            Permanent permanent = placement.preparedPermanent();
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, placement.controllerId(), permanent, enterTappedTypes, simultaneouslyEntered);
            if (batch.enterWithCounter() != null && !gameQueryService.cantHaveCounters(gameData, permanent)) {
                // Entry counters are set before any state-based action runs; a permanent that can't have
                // counters (Tatterkite) enters without the additional counter.
                permanent.setCounterCount(batch.enterWithCounter(),
                        permanent.getCounterCount(batch.enterWithCounter()) + 1);
            }
            // A card taken from another player's graveyard still belongs to that player (Ghouls' Night Out).
            UUID graveyardOwnerId = permanent.getEnteredFromGraveyardOwnerId();
            if (graveyardOwnerId != null && !graveyardOwnerId.equals(placement.controllerId())
                    && !gameData.stolenCreatures.containsKey(permanent.getId())) {
                trackStolenCreature(gameData, permanent.getId(), placement.controllerId(), graveyardOwnerId);
            }
            simultaneouslyEntered.add(permanent);
            enteredPermanents.add(new ReturnedPermanent(placement.controllerId(), permanent, placement.auraCard()));
        }

        for (ReturnedPermanent returned : enteredPermanents) {
            handleCreatureEtbAndLegendRule(
                    gameData, returned.controllerId(), returned.permanent(), returned.card());
        }
    }

    private List<UUID> findAuraAttachmentCandidates(GameData gameData, Card auraCard, UUID auraControllerId) {
        List<UUID> attachments = new ArrayList<>();
        gameData.forEachPermanent((ignored, candidate) -> {
            if (auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, candidate)) {
                attachments.add(candidate.getId());
            }
        });
        if (auraCard.isEnchantPlayer()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (auraAttachmentService.canEnchantPlayer(gameData, auraCard, auraControllerId, playerId)) {
                    attachments.add(playerId);
                }
            }
        }
        return attachments;
    }

    /** Puts selected cards from the graveyard and exile onto the battlefield simultaneously. */
    public void putCardsFromGraveyardAndExileOntoBattlefieldSimultaneously(
            GameData gameData, UUID controllerId, List<Card> graveyardCards, List<Card> exiledCards) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<ReturnedPermanent> enteredPermanents = new ArrayList<>();

        for (Card card : graveyardCards) {
            if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                UUID graveyardOwnerId = card.getOwnerId() != null ? card.getOwnerId() : controllerId;
                gameData.playerGraveyards.computeIfAbsent(graveyardOwnerId, ignored -> new ArrayList<>()).add(card);
                continue;
            }
            Permanent permanent = new Permanent(card);
                permanent.setEnteredFromGraveyardOwnerId(controllerId);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            enteredPermanents.add(new ReturnedPermanent(controllerId, permanent, card));
        }

        for (Card card : exiledCards) {
            if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.EXILE)) {
                exileService.exileCard(gameData, controllerId, card);
                continue;
            }
            Permanent permanent = new Permanent(card);
                battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            enteredPermanents.add(new ReturnedPermanent(controllerId, permanent, card));
        }

        for (ReturnedPermanent returned : enteredPermanents) {
            handleCreatureEtbAndLegendRule(gameData, returned.controllerId(), returned.permanent(), returned.card());
        }
    }

    private record ReturnedPermanent(UUID controllerId, Permanent permanent, Card card) {}

    /**
     * The permanents of the same return-all batch that already entered. They enter simultaneously with the
     * next one, so none of them may see it enter or apply their replacement effects to it (CR 614.12).
     */
    private static List<Permanent> enteredBatchMates(List<ReturnedPermanent> deferredEtbs) {
        return deferredEtbs == null ? List.of()
                : deferredEtbs.stream().map(ReturnedPermanent::permanent).toList();
    }

    /**
     * Reanimates a targeted graveyard {@code card} under {@code controllerId}: removes it from
     * whichever graveyard it is in and puts it onto the battlefield. Returns the created permanent,
     * or {@code null} if it was blocked from entering (e.g. Grafdigger's Cage). Used by reanimation
     * Auras (Animate Dead) that must attach themselves to the returned creature.
     */
    public Permanent reanimateTargetedCard(GameData gameData, UUID controllerId, Card card) {
        return reanimateTargetedCard(gameData, controllerId, card, false);
    }

    /**
     * Same as {@link #reanimateTargetedCard(GameData, UUID, Card)}, but the returned permanent
     * enters tapped when {@code enterTapped} is true (Dance of the Dead).
     */
    public Permanent reanimateTargetedCard(GameData gameData, UUID controllerId, Card card, boolean enterTapped) {
        return reanimateTargetedCard(gameData, controllerId, card, enterTapped, permanent -> { });
    }

    /**
     * Reanimates a targeted card after allowing linked objects to adopt the new permanent ID before
     * battlefield-entry processing begins. Reanimation Auras use this to remain continuously
     * attached while their enchanted card changes zones.
     */
    public Permanent reanimateTargetedCard(GameData gameData, UUID controllerId, Card card,
                                             boolean enterTapped, Consumer<Permanent> beforeEntry) {
        if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            return null;
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        return putCardOntoBattlefield(gameData, controllerId, card, null, null, enterTapped, false,
                null, false, false, 0, permanent -> {
                    permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
                    if (graveyardOwnerId != null && !controllerId.equals(graveyardOwnerId)) {
                        trackStolenCreature(gameData, permanent.getId(), controllerId, graveyardOwnerId);
                    }
                    beforeEntry.accept(permanent);
                });
    }

    /**
     * Destroys each targeted permanent and returns each card actually put into a graveyard this way
     * to the battlefield under the controller's control. Indestructible or regenerated permanents are
     * not returned.
     */

    /**
     * Resolves an {@link UndyingReturnEffect} (CR 702.93) by returning the dying card from its owner's
     * graveyard to the battlefield under its owner's control with a +1/+1 counter on it. The entry is
     * flagged as entering from the owner's graveyard so "enters from your graveyard" triggers (e.g.
     * Flayer of the Hatebound) fire. Fizzles silently if the card is no longer in a graveyard.
     */

    /**
     * Returns {@code true} if the given card is currently barred from entering the battlefield from
     * {@code zone} (e.g. a creature card while Grafdigger's Cage is on the battlefield).
     */
    public boolean isCardBlockedFromEnteringFromZone(GameData gameData, Card card, Zone zone) {
        return gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, zone);
    }

    public void applyPermanentGrants(Permanent permanent, CardColor grantColor, CardSubtype grantSubtype) {
        applyPermanentGrants(permanent, grantColor, grantSubtype, false);
    }

    public void applyPermanentGrants(Permanent permanent, CardColor grantColor, CardSubtype grantSubtype,
                                     boolean grantIndestructible) {
        if (grantColor != null) {
            permanent.getGrantedColors().add(grantColor);
        }
        if (grantSubtype != null && !permanent.getGrantedSubtypes().contains(grantSubtype)) {
            permanent.getGrantedSubtypes().add(grantSubtype);
        }
        if (grantIndestructible) {
            permanent.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        }
    }

    /** Shallow Grave: haste granted on return lasts only until end of turn, so it moves to the cleanup-cleared set. */
    private static void limitHasteToThisTurn(ReturnCardFromGraveyardEffect effect, Permanent returnedPermanent) {
        if (returnedPermanent != null && effect.grantHaste() && effect.hasteUntilEndOfTurn()
                && returnedPermanent.getPersistentGrantedKeywords().remove(Keyword.HASTE)) {
            returnedPermanent.getGrantedKeywords().add(Keyword.HASTE);
        }
    }

    public Permanent putCardOntoBattlefieldWithHasteAndExile(GameData gameData, UUID controllerId, Card card,
                                                              boolean grantHaste, boolean exileAtEndStep,
                                                              boolean sacrificeAtEndStep,
                                                              boolean exileIfLeavesBattlefield) {
        return putCardOntoBattlefieldWithHasteAndExile(gameData, controllerId, card, grantHaste, exileAtEndStep,
                sacrificeAtEndStep, exileIfLeavesBattlefield, false, false);
    }

    public Permanent putCardOntoBattlefieldWithHasteAndExile(GameData gameData, UUID controllerId, Card card,
                                                              boolean grantHaste, boolean exileAtEndStep,
                                                              boolean sacrificeAtEndStep,
                                                              boolean exileIfLeavesBattlefield,
                                                              boolean enterTapped, boolean enterAttacking) {
        return putCardOntoBattlefieldWithHasteAndExile(gameData, controllerId, card, grantHaste, exileAtEndStep,
                sacrificeAtEndStep, exileIfLeavesBattlefield, enterTapped, enterAttacking, false);
    }

    public Permanent putCardOntoBattlefieldWithHasteAndExile(GameData gameData, UUID controllerId, Card card,
                                                              boolean grantHaste, boolean exileAtEndStep,
                                                              boolean sacrificeAtEndStep,
                                                              boolean exileIfLeavesBattlefield,
                                                              boolean enterTapped, boolean enterAttacking,
                                                              boolean exileAtYourNextEndStep) {
        return putCardOntoBattlefieldWithHasteAndExile(gameData, controllerId, card, grantHaste, exileAtEndStep,
                sacrificeAtEndStep, exileIfLeavesBattlefield, enterTapped, enterAttacking,
                exileAtYourNextEndStep, false);
    }

    public Permanent putCardOntoBattlefieldWithHasteAndExile(GameData gameData, UUID controllerId, Card card,
                                                              boolean grantHaste, boolean exileAtEndStep,
                                                              boolean sacrificeAtEndStep,
                                                              boolean exileIfLeavesBattlefield,
                                                              boolean enterTapped, boolean enterAttacking,
                                                              boolean exileAtYourNextEndStep,
                                                              boolean losesAllAbilities) {
        return putCardOntoBattlefieldWithHasteAndExile(gameData, controllerId, card, grantHaste, exileAtEndStep,
                sacrificeAtEndStep, exileIfLeavesBattlefield, enterTapped, enterAttacking,
                exileAtYourNextEndStep, losesAllAbilities, null);
    }

    /**
     * {@code delayedTriggerSource} is the spell or ability whose delayed "sacrifice it at the beginning of
     * the next end step" trigger this is; when null the returned permanent's own card is the source.
     */
    public Permanent putCardOntoBattlefieldWithHasteAndExile(GameData gameData, UUID controllerId, Card card,
                                                              boolean grantHaste, boolean exileAtEndStep,
                                                              boolean sacrificeAtEndStep,
                                                              boolean exileIfLeavesBattlefield,
                                                              boolean enterTapped, boolean enterAttacking,
                                                              boolean exileAtYourNextEndStep,
                                                              boolean losesAllAbilities,
                                                              Card delayedTriggerSource) {
        return putCardOntoBattlefieldWithHasteAndExile(gameData, controllerId, card, grantHaste, exileAtEndStep,
                sacrificeAtEndStep, exileIfLeavesBattlefield, enterTapped, enterAttacking,
                exileAtYourNextEndStep, losesAllAbilities, delayedTriggerSource, null);
    }

    /**
     * When {@code deferredEtbs} is non-null the enter-the-battlefield handling is left to the caller so
     * every card of one return-all batch enters simultaneously (CR 603.6a).
     */
    private Permanent putCardOntoBattlefieldWithHasteAndExile(GameData gameData, UUID controllerId, Card card,
                                                              boolean grantHaste, boolean exileAtEndStep,
                                                              boolean sacrificeAtEndStep,
                                                              boolean exileIfLeavesBattlefield,
                                                              boolean enterTapped, boolean enterAttacking,
                                                              boolean exileAtYourNextEndStep,
                                                              boolean losesAllAbilities,
                                                              Card delayedTriggerSource,
                                                              List<ReturnedPermanent> deferredEtbs) {
        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield.
        if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameData.playerGraveyards.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(card);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " can't put ", card,
                    " onto the battlefield from a graveyard; it stays in the graveyard."));
            log.info("Game {} - {} blocked from entering the battlefield from a graveyard", gameData.id, card.getName());
            return null;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        permanent.setLosesAllAbilitiesPermanently(losesAllAbilities);
        if (grantHaste) {
            permanent.getPersistentGrantedKeywords().add(Keyword.HASTE);
        }
        if (enterTapped) {
            permanent.tap();
        }
        permanent.setEnteredFromZone(Zone.GRAVEYARD);
        permanent.setEnteredFromGraveyardOwnerId(controllerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes,
                enteredBatchMates(deferredEtbs));
        if (enterTapped) {
            permanent.tap();
        }
        if (enterAttacking) {
            permanent.enterAttacking(true);
        }
        if (exileIfLeavesBattlefield) {
            // Unearth's second clause (CR 702.84a): if it would leave the battlefield for any other
            // reason, exile it instead. Shallow Grave has only the delayed exile, not this.
            permanent.setExileIfLeavesBattlefield(true);
        }
        if (exileAtEndStep || exileAtYourNextEndStep) {
            gameData.queueDelayedAction(exileAtYourNextEndStep
                    ? new DelayedPermanentAction(permanent.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP,
                    false, null, controllerId, null, false, delayedTriggerSource)
                    : new DelayedPermanentAction(permanent.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP,
                    false, null, null, null, false, delayedTriggerSource));
        }
        if (sacrificeAtEndStep) {
            gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(),
                    DelayedPermanentActionKind.SACRIFICE_AT_END_STEP, false, null, null, controllerId, false,
                    delayedTriggerSource));
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String hasteText = grantHaste ? " with haste" : "";
        gameLogService.append(gameData, GameLog.textCardText(playerName + " returns ", card,
                " to the battlefield" + hasteText + "."));

        if (deferredEtbs != null) {
            deferredEtbs.add(new ReturnedPermanent(controllerId, permanent, card));
        } else {
            handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
        }
        return permanent;
    }

    /**
     * Moves each pre-targeted card from whichever graveyard still holds it to the top of that
     * graveyard owner's library, in target order (so the last processed card ends up on top).
     * Used by "put target cards from an opponent's graveyard on top of their library"
     * (Misinformation). Cards that already left the graveyard are silently skipped.
     */
    public void putTargetedCardsFromAnyGraveyardOnTopOfLibrary(GameData gameData, StackEntry entry) {
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        List<Card> movedCards = new ArrayList<>();
        UUID ownerId = null;
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card == null) {
                    continue;
                }
                for (UUID pid : gameData.orderedPlayerIds) {
                    if (entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                            && !pid.equals(entry.getTargetId())) {
                        continue;
                    }
                    List<Card> graveyard = gameData.playerGraveyards.get(pid);
                    if (graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                        graveyardService.notifyCardsLeftGraveyard(gameData, pid, card);
                        movedCards.add(card);
                        ownerId = pid;
                        break;
                    }
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!movedCards.isEmpty()) {
            if (movedCards.size() == 1) {
                gameData.playerDecks.get(ownerId).addFirst(movedCards.getFirst());
            } else {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                        entry.getControllerId(), movedCards, false, ownerId,
                        "Put these cards on top of their owner's library in any order."));
            }
            String playerName = gameData.playerIdToName.get(ownerId);
            GameLog.Builder builder = GameLog.builder().text(playerName + " has ");
            appendCardList(builder, movedCards);
            builder.text(" put on top of their library from their graveyard.");
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} card(s) put from {}'s graveyard on top of their library",
                    gameData.id, movedCards.size(), playerName);
        }
    }

    public boolean exileCardFromAnyGraveyard(GameData gameData, UUID cardId, Card card) {
        return exileCardFromAnyGraveyard(gameData, cardId, card, null);
    }

    public boolean exileCardFromAnyGraveyard(GameData gameData, UUID cardId, Card card,
                                             UUID sourcePermanentId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                graveyardService.notifyCardsExiledFromGraveyard(gameData, pid, card);
                if (sourcePermanentId == null) {
                    exileService.exileCard(gameData, pid, card);
                } else {
                    exileService.exileCard(gameData, pid, card, sourcePermanentId);
                }
                return true;
            }
        }
        return false;
    }

    public void handleCreatureEtbAndLegendRule(GameData gameData, UUID controllerId, Permanent permanent, Card card) {
        // Runs even when the creature lost its abilities: only its own enter abilities are suppressed
        // (by the ETB service), while other permanents still see a creature enter.
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    public void applyLifeGainEqualToManaValue(GameData gameData, UUID controllerId, Card card) {
        int manaValue = card.getManaValue();
        if (manaValue > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, manaValue);
        }
    }

    public void applyLifeGainEqualToReturnedToughness(GameData gameData, UUID controllerId,
                                                       Permanent returnedPermanent) {
        int toughness = gameQueryService.getEffectiveToughness(gameData, returnedPermanent);
        if (toughness > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, toughness);
        }
    }

    /**
     * Reanimate's rider: the spell's controller loses life equal to the returned card's mana value, after the
     * return (CR 608.2c). On a resolving entry the loss is a step of its own, so a Lich's Duel Mastery sacrifice
     * replacing it asks its CR 616.1 questions without returning the card again.
     */
    public void applyLifeLossEqualToManaValue(GameData gameData, StackEntry entry, UUID controllerId, Card card) {
        int manaValue = card.getManaValue();
        if (manaValue <= 0) {
            return;
        }
        if (entry.getResolvingEffectIndex() >= 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new PlayersLoseLifeEffect(Map.of(controllerId, manaValue))));
        } else {
            lifeSupport.applyLifeLoss(gameData, controllerId, manaValue, entry.getCard().getName());
        }
    }

    /**
     * Records that a permanent entered the battlefield under a non-owner's control (e.g. stolen
     * from an opponent's graveyard): stamps the ownership record and creates the indefinite
     * floating control effect that keeps it with {@code controllerId}.
     */
    public void trackStolenCreature(GameData gameData, UUID permanentId, UUID controllerId, UUID originalOwnerId) {
        gameData.stolenCreatures.put(permanentId, originalOwnerId);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), null, null, controllerId,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                permanentId, null, null, EffectDuration.PERMANENT, 0));
    }

    public record StolenCreatureResult(Permanent permanent, Card card, UUID originalOwnerId) {}

    public StolenCreatureResult stealFromOpponentGraveyard(GameData gameData, StackEntry entry, UUID controllerId) {
        return stealFromGraveyard(gameData, entry, controllerId, true);
    }

    /**
     * Takes the entry's targeted card out of a graveyard for {@code controllerId} to put onto the
     * battlefield. With {@code opponentOnly} the card must sit in an opponent's graveyard and the
     * entry fizzles otherwise; without it any graveyard qualifies, including the controller's own
     * (Lim-Dul the Necromancer returns "that card" whoever owned it).
     */
    public StolenCreatureResult stealFromGraveyard(GameData gameData, StackEntry entry, UUID controllerId,
                                                   boolean opponentOnly) {
        // SPELL-slot casts (Gruesome Encore) carry the target on targetId; ETB triggers
        // (Puppeteer Clique) choose it via a multi-graveyard choice, landing it on targetCardIds.
        UUID targetId = entry.getTargetId() != null ? entry.getTargetId()
                : (entry.getTargetCardIds().isEmpty() ? null : entry.getTargetCardIds().getFirst());
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        if (targetCard == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in graveyard)."));
            return null;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null || (opponentOnly && graveyardOwnerId.equals(controllerId))) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target not in "
                    + (opponentOnly ? "opponent's" : "any") + " graveyard)."));
            return null;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
        Permanent permanent = new Permanent(targetCard);
        return new StolenCreatureResult(permanent, targetCard, graveyardOwnerId);
    }

    /**
     * Resolves a {@link PutCardFromOpponentGraveyardOntoBattlefieldEffect} by stealing a targeted
     * creature from an opponent's graveyard and putting it onto the battlefield under the controller's
     * control. Optionally enters tapped. If the stack entry has a positive X value, the opponent
     * also mills that many cards. Fizzles if the target is no longer in an opponent's graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (X value determines mill count)
     * @param effect   the steal-from-opponent effect configuration
     */

    /**
     * Resolves a {@link PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect} by stealing
     * a targeted creature from an opponent's graveyard and putting it onto the battlefield with haste.
     * The creature is exiled at the next end step. Fizzles if the target is no longer in an
     * opponent's graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     */

    /**
     * Resolves an {@link ExileCardsFromGraveyardEffect} by exiling each pre-targeted card that is
     * still in a graveyard. After exiling, the controller gains life if the effect specifies a
     * positive life gain amount.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the exile effect configuration (includes life gain amount)
     */

    /**
     * Resolves an {@link ExileTargetCardFromGraveyardAndImprintOnSourceEffect} by exiling a targeted
     * card from a graveyard and tracking it as imprinted on the source permanent. Validates the card
     * still matches the required type (if any). Fizzles if the target is no longer in a graveyard or
     * no longer matches the type requirement.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (source permanent receives the imprint)
     * @param effect   the imprint effect configuration (includes optional type requirement)
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling a targeted card from a
     * graveyard. Unlike the imprint variant, this does NOT track the exiled card on the source
     * permanent. Validates the card still matches the required type (if any). Fizzles if the target
     * is no longer in a graveyard or no longer matches the type requirement.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the exile effect configuration (includes optional type requirement)
     */

    /**
     * Resolves an {@link ExileGraveyardCardWithConditionalBonusEffect} by exiling the targeted card
     * from a graveyard, then conditionally gaining life (if the exiled card is a creature) or
     * boosting the source permanent (if the exiled card is a noncreature).
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     * @param effect   the effect configuration (life gain amount, power/toughness boost amounts)
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling
     * the pre-targeted cards from an opponent's graveyard. Reads target card IDs from
     * {@code entry.getTargetCardIds()}. Cards that are no longer in a graveyard at
     * resolution time are silently skipped.
     */

    /**
     * Resolves an {@link ExileCreaturesFromGraveyardAndCreateTokensEffect} by exiling each pre-targeted
     * creature card from graveyards and creating a 2/2 black Zombie creature token for each card
     * successfully exiled.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling all cards in the target
     * player's graveyard. Does nothing beyond logging if the graveyard is already empty.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (target player ID is in targetId)
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by exiling all cards from every
     * opponent's graveyard. Does not affect the controller's graveyard.
     */

    /**
     * Resolves a {@link ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect} by exiling all
     * non-basic-land cards from the target player's graveyard, then searching that player's library
     * for all cards with the same name as any card exiled this way and exiling them too.
     * Finally, the target player shuffles their library.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (target player ID is in targetId)
     */

    /**
     * Resolves a {@link PutImprintedCreatureOntoBattlefieldEffect} by revealing the card imprinted
     * on the source and, if it is a creature card, putting it onto the battlefield under the
     * controller's control. Non-creature cards remain in exile.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (card must have an imprinted card)
     */

    /**
     * Resolves a {@link PutImprintedCardIntoOwnersHandEffect} by putting the card imprinted
     * on the source into its owner's hand. The owner is determined by which player's exile zone
     * contains the card. Used by cards like Hoarding Dragon.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (card must have an imprinted card)
     */

    /**
     * Resolves a {@link ReturnDyingCreatureToBattlefieldEffect} by returning a
     * creature that just died back to the battlefield and attaching the source equipment to it.
     * Used by equipment with triggered abilities like Nim Deathmantle. Fizzles if the dying
     * card is no longer in a graveyard.
     *
     * @param gameData the current game state
     * @param entry    the stack entry being resolved (targetId is the source equipment)
     * @param effect   the return-and-attach effect (dyingCardId identifies the creature to return)
     */

    /**
     * Resolves a {@link CastTargetInstantOrSorceryFromGraveyardEffect} by
     * validating the targeted instant or sorcery card is still in a graveyard matching the scope,
     * then queuing a may-cast choice for the controller.
     */

    /**
     * Resolves an {@link ExileGraveyardCardsEffect} by forcing the affected
     * player to exile cards from their own graveyard.
     * <ul>
     *   <li>0 cards in graveyard: nothing happens</li>
     *   <li>1 to count cards: all are auto-exiled</li>
     *   <li>More than count cards: the player chooses which to exile</li>
     * </ul>
     */

    /**
     * Resolves a {@link TargetPlayerExilesCardFromGraveyardEffect} by forcing the target player
     * to exile a card from their graveyard. If the graveyard has only one card, it is auto-exiled.
     * If the exiled card is a creature and {@code lifeGainIfCreature > 0}, the ability's controller
     * gains that much life.
     */

    /**
     * Sets up a graveyard choice interaction for the player to choose a card to exile.
     * If multiple exiles are needed, the remaining count is tracked so subsequent choices
     * are presented after each exile.
     */
    public void beginGraveyardExileChoice(GameData gameData, UUID playerId, int remainingCount) {
        beginGraveyardExileChoice(gameData, playerId, remainingCount, null);
    }

    /**
     * Variant of {@link #beginGraveyardExileChoice(GameData, UUID, int)} that excludes a specific
     * card from the choice (by identity). Used when a spell resolving with a "you may exile a card
     * from your graveyard" clause has already been placed into its owner's graveyard — that spell
     * card is not actually in the graveyard yet per the rules and must not be a valid choice.
     */
    public void beginGraveyardExileChoice(GameData gameData, UUID playerId, int remainingCount, Card excludedCard) {
        beginGraveyardExileChoice(gameData, playerId, remainingCount, excludedCard, List.of());
    }

    /**
     * Variant that queues {@code afterExileEffects} into the paused resolution once the card has
     * actually been exiled ("exile a card from your graveyard. If you do, ...").
     */
    public void beginGraveyardExileChoice(GameData gameData, UUID playerId, int remainingCount, Card excludedCard,
                                          List<CardEffect> afterExileEffects) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> validIndices = IntStream.range(0, graveyard.size())
                .filter(i -> excludedCard == null || graveyard.get(i) != excludedCard)
                .boxed().toList();

        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(playerId, validIndices, GraveyardChoiceDestination.EXILE,
                        "Choose a card to exile from your graveyard.")
                .exileRemainingCount(remainingCount);
        if (!afterExileEffects.isEmpty()) {
            choice.mayAbilityContext(null, playerId, afterExileEffects, null);
        }
        interactionHandlerRegistry.begin(gameData, choice.build());
    }

    /**
     * Resolves an {@link EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect}. Each player
     * returns up to {@code maxCount} matching cards from their graveyard to the battlefield.
     * If a player has fewer matching cards than {@code maxCount}, all are returned automatically.
     * If a player has more, they are prompted to choose via the graveyard choice queue.
     */

    /**
     * Resolves a {@link ReturnOneOfEachSubtypeFromGraveyardToHandEffect} by queuing sequential
     * graveyard choices — one per subtype. For each subtype that has matching cards in the
     * controller's graveyard, a choice entry is queued. If only one card matches a subtype,
     * it is returned automatically without prompting. Cards already returned for a previous
     * subtype are naturally excluded because they have been removed from the graveyard.
     */

    /**
     * Starts the queued graveyard choices of one effect as a single graveyard-leave event, so every
     * card they move counts as leaving together for "one or more cards leave your graveyard" triggers.
     * The event ends once the queue runs out.
     */
    public void beginGraveyardReturnQueueAsOneEvent(GameData gameData) {
        if (!gameData.graveyardReturnQueueLeaveBatchOpen) {
            gameData.graveyardReturnQueueLeaveBatchOpen = true;
            graveyardService.beginGraveyardLeaveBatch(gameData);
        }
        beginNextGraveyardReturnFromQueue(gameData);
    }

    /** Moves every card chosen during a simultaneous "each player returns" flow from its graveyard to its player's hand. */
    private void releaseHeldHandReturns(GameData gameData) {
        gameData.holdGraveyardHandReturns = false;
        if (gameData.heldGraveyardHandReturns.isEmpty()) {
            return;
        }
        Map<UUID, UUID> held = new LinkedHashMap<>(gameData.heldGraveyardHandReturns);
        gameData.heldGraveyardHandReturns.clear();
        for (Map.Entry<UUID, UUID> heldEntry : held.entrySet()) {
            UUID playerId = heldEntry.getValue();
            Card card = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(c -> c.getId().equals(heldEntry.getKey()))
                    .findFirst()
                    .orElse(null);
            if (card == null) {
                continue;
            }
            permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
            permanentRemovalService.addCardToHandFromGraveyard(gameData, playerId, playerId, card);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " returns ", card, " from graveyard to hand."));
        }
    }

    /**
     * Pops the next entry from the graveyard return queue and prompts that player to choose
     * a card to return from their graveyard. Does nothing if the queue is empty.
     */
    public void beginNextGraveyardReturnFromQueue(GameData gameData) {
        if (gameData.pendingGraveyardReturnQueue.isEmpty()) {
            if (gameData.pendingGraveyardReturnBatch != null) {
                PendingGraveyardReturnBatch batch = gameData.pendingGraveyardReturnBatch;
                gameData.pendingGraveyardReturnBatch = null;
                if (batch.holdInGraveyard()) {
                    battlefieldEntryBatchSupport.begin(gameData, batch.cards().stream()
                            .map(card -> new BattlefieldEntryCard(batch.controllerId(),
                                    batch.graveyardOwnerByCardId().get(card.getId()), card, Zone.GRAVEYARD, null))
                            .toList());
                } else {
                    putCardsOntoBattlefieldSimultaneouslyUnderController(gameData, batch);
                }
            }
            releaseHeldHandReturns(gameData);
            if (gameData.graveyardReturnQueueLeaveBatchOpen) {
                gameData.graveyardReturnQueueLeaveBatchOpen = false;
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
            return;
        }

        PendingGraveyardReturnChoice next = gameData.pendingGraveyardReturnQueue.removeFirst();
        List<Card> graveyard = gameData.playerGraveyards.get(next.playerId());
        if (graveyard == null || graveyard.isEmpty()) {
            // Player's graveyard is empty — skip to next
            beginNextGraveyardReturnFromQueue(gameData);
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        Set<UUID> trackedIds = next.fromBattlefieldThisTurn()
                ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(next.playerId(), Set.of())
                : null;
        for (int i = 0; i < graveyard.size(); i++) {
            if ((trackedIds == null || trackedIds.contains(graveyard.get(i).getId()))
                    && predicateEvaluationService.matchesCardPredicate(graveyard.get(i), next.filter(), null,
                            gameData, next.playerId())
                    && !next.excludedManaValues().contains(graveyard.get(i).getManaValue())
                    && !next.excludedCardIds().contains(graveyard.get(i).getId())) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            // No matching cards left — skip to next
            beginNextGraveyardReturnFromQueue(gameData);
            return;
        }

        // Re-enqueue with decremented remaining count if the player has more picks after this one
        if (next.remainingCount() > 1) {
            gameData.pendingGraveyardReturnQueue.addFirst(
                    new PendingGraveyardReturnChoice(next.playerId(), next.remainingCount() - 1, next.filter(),
                            next.destination(), next.skipRemainingOnDecline(), next.mandatory(),
                            next.fromBattlefieldThisTurn(), next.distinctManaValues(),
                            next.distinctNames(), next.excludedManaValues(), next.excludedCardIds(),
                            next.choosingPlayerId()));
        }

        GraveyardChoiceDestination destination = next.destination();
        String filterLabel = CardPredicateUtils.describeFilter(next.filter());
        String destText = destination == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield";
        PendingGraveyardReturnBatch batch = gameData.pendingGraveyardReturnBatch;
        UUID choosingPlayerId = next.choosingPlayerId() != null
                ? next.choosingPlayerId()
                : batch == null || batch.eachPlayerChooses() ? next.playerId() : batch.controllerId();
        List<Card> matchingCards = matchingIndices.stream().map(graveyard::get).toList();
        // A held batch lets the controller pick from the graveyard itself; an opponent choosing from the
        // controller's graveyard needs the matching cards as a pool, since indices would address their own.
        boolean choosesFromPool = batch != null
                && (!batch.holdInGraveyard() || !choosingPlayerId.equals(next.playerId()));
        List<Integer> choiceIndices = !choosesFromPool
                ? matchingIndices
                : IntStream.range(0, matchingCards.size()).boxed().toList();
        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(choosingPlayerId, choiceIndices, destination,
                        "Return a " + filterLabel + " from "
                                + gameData.playerIdToName.get(next.playerId()) + "'s graveyard to " + destText + ".")
                .mandatory(next.mandatory());
        if (choosesFromPool) {
            choice.cardPool(matchingCards);
        }
        interactionHandlerRegistry.begin(gameData, choice.build());
    }

    /**
     * Registers a delayed trigger that will return the specified card from its owner's graveyard
     * to their hand at the beginning of the next end step. The card must still be in the graveyard
     * when the end step arrives for the return to happen.
     *
     * <p>Used by Tiana, Ship's Caretaker.
     */

    /**
     * Registers a delayed trigger that will return the source card from its owner's graveyard
     * to the battlefield transformed at the beginning of the next end step.
     */

    /**
     * Step 1: the separator has assigned cards to Pile 1. Unselected cards form Pile 2.
     * Prompt the appropriate player to choose which pile receives the disposition.
     */
    public void completeCardPileSeparationStep1(GameData gameData, List<UUID> pile1CardIds) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);

        List<UUID> pile1 = new ArrayList<>(state.pile1Ids());
        pile1.addAll(pile1CardIds);
        // Pile 2 is everything not in Pile 1
        List<UUID> pile2 = new ArrayList<>(state.pile2Ids());
        for (Card card : state.cards()) {
            if (!pile1CardIds.contains(card.getId())) {
                pile2.add(card.getId());
            }
        }

        if (state.disposition() == CardPileDisposition.HAND_AND_EXILE_WITH_FACE_DOWN_PILE) {
            // Both piles are exiled before the chooser picks one (The Celestial Toymaker): the chosen pile
            // is then taken from exile into hand, and the other pile stays exiled.
            boolean pile1FaceDown = state.controllerChoosesPile();
            for (Card card : state.cards()) {
                UUID ownerId = state.cardOwners().getOrDefault(card.getId(), state.controllerId());
                boolean faceDown = pile1FaceDown == pile1.contains(card.getId());
                gameData.addToExile(ownerId, card, null, faceDown);
            }
        }

        // Re-queue with the piles filled — step 2 (the pile-choice may prompt) polls it. Preserve the
        // disposition so BATTLEFIELD (Boneyard Parley), HAND (Unesh), and Fortune's Favor's
        // face-down/face-up variant all survive the re-queue.
        gameData.queueInteraction(new PendingPileSeparation(state.controllerId(), state.targetPlayerId(),
                state.allPermanentIds(), state.cards(), state.cardOwners(), pile1, pile2, state.disposition(),
                state.controllerChoosesPile()));

        // Phyrexian Portal's piles are both face down. For the shared one-face-down flow,
        // Fortune's Favor uses a face-down Pile 1 while Curator of Destinies uses a face-down Pile 2.
        boolean bothPilesFaceDown = state.disposition() == CardPileDisposition.SEARCH_ONE_TO_HAND;
        boolean onePileFaceDown = state.disposition() == CardPileDisposition.HAND_WITH_FACE_DOWN_PILE
                || state.disposition() == CardPileDisposition.HAND_AND_EXILE_WITH_FACE_DOWN_PILE
                || state.disposition() == CardPileDisposition.HAND_AND_BOTTOM_WITH_FACE_DOWN_PILE;
        boolean pile1FaceDown = bothPilesFaceDown || onePileFaceDown && state.controllerChoosesPile();
        boolean pile2FaceDown = bothPilesFaceDown || onePileFaceDown && !state.controllerChoosesPile();
        String pile1Desc = pile1FaceDown ? describePileSize(pile1) : buildCardPileDescription(state.cards(), pile1);
        String pile2Desc = pile2FaceDown ? describePileSize(pile2) : buildCardPileDescription(state.cards(), pile2);

        UUID separatorId = state.controllerChoosesPile() ? state.targetPlayerId() : state.controllerId();
        UUID chooserId = state.controllerChoosesPile() ? state.controllerId() : state.targetPlayerId();
        String separatorName = gameData.playerIdToName.get(separatorId);
        if (bothPilesFaceDown) {
            gameLogService.append(gameData, GameLog.text(separatorName
                    + " separates the cards into two face-down piles. Pile 1: " + pile1Desc
                    + ". Pile 2: " + pile2Desc + "."));
        } else if (onePileFaceDown) {
            GameLog.Builder pileLog = GameLog.builder().text(separatorName
                    + " separates the cards into a face-down pile and a face-up pile. ");
            if (pile1FaceDown) {
                pileLog.text("Face-down pile: ").text(pile1Desc).text(". Face-up pile: ");
                appendCardPile(pileLog, state.cards(), pile2);
            } else {
                pileLog.text("Face-up pile: ");
                appendCardPile(pileLog, state.cards(), pile1);
                pileLog.text(". Face-down pile: ").text(pile2Desc);
            }
            pileLog.text(".");
            gameLogService.append(gameData, pileLog.build());
        } else {
            GameLog.Builder pileLog = GameLog.builder().text(separatorName + " separates cards into two piles. Pile 1: ");
            appendCardPile(pileLog, state.cards(), pile1);
            pileLog.text(". Pile 2: ");
            appendCardPile(pileLog, state.cards(), pile2);
            pileLog.text(".");
            gameLogService.append(gameData, pileLog.build());
        }

        UUID controllerId = state.controllerId();
        String destText = switch (state.disposition()) {
            case HAND, HAND_AND_THOPTER, HAND_WITH_FACE_DOWN_PILE, HAND_AND_EXILE_WITH_FACE_DOWN_PILE,
                    HAND_AND_BOTTOM, HAND_AND_BOTTOM_WITH_FACE_DOWN_PILE -> "put into your hand";
            case ONE_FROM_CHOSEN_HAND_AND_BOTTOM -> "put one card into your hand";
            case SEARCH_ONE_TO_HAND -> "search (the other pile is exiled)";
            case OPPONENT_CHOOSES_EXILE -> "exile";
            default -> "put onto the battlefield";
        };
        String prompt = onePileFaceDown
                ? "Choose a pile to " + destText + ". Yes = "
                + (pile1FaceDown ? "face-down" : "face-up") + " pile (" + pile1Desc
                + "), No = " + (pile2FaceDown ? "face-down" : "face-up") + " pile (" + pile2Desc + ")."
                : "Choose a pile to " + destText + ". Yes = Pile 1 (" + pile1Desc + "), No = Pile 2 (" + pile2Desc + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, chooserId, List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Step 2: the chooser has selected a pile. Apply the disposition to the chosen pile and send the
     * other pile to the appropriate destination.
     */
    public void completeCardPileSeparationStep2(GameData gameData, boolean accepted) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        List<UUID> chosenPileCardIds = accepted
                ? new ArrayList<>(state.pile1Ids())
                : new ArrayList<>(state.pile2Ids());
        List<UUID> otherPileCardIds = accepted
                ? new ArrayList<>(state.pile2Ids())
                : new ArrayList<>(state.pile1Ids());
        String chosenPileName = accepted ? "Pile 1" : "Pile 2";

        UUID controllerId = state.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        List<Card> allCards = new ArrayList<>(state.cards());
        Map<UUID, UUID> cardOwners = new HashMap<>(state.cardOwners());

        
        

        UUID chooserId = state.controllerChoosesPile() ? controllerId : state.targetPlayerId();
        String chooserName = gameData.playerIdToName.get(chooserId);
        gameLogService.append(gameData, GameLog.text(chooserName + " chooses " + chosenPileName + "."));
        if (state.disposition() == CardPileDisposition.GRAVEYARD_AND_FREE_CAST_ONE_REST_TO_HAND) {
            completeAbstractPerformancePileChoice(gameData, state, chosenPileCardIds, otherPileCardIds, allCards);
            return;
        }
        if (state.disposition() == CardPileDisposition.OPPONENT_CHOOSES_EXILE) {
            completeDeathOrGloryPileChoice(gameData, state, chosenPileCardIds, otherPileCardIds, allCards, cardOwners,
                    accepted);
            return;
        }

        gameLogService.append(gameData, GameLog.text(controllerName + " chooses " + chosenPileName + "."));

        if (state.disposition() == CardPileDisposition.HAND_AND_BOTTOM
                || state.disposition() == CardPileDisposition.HAND_AND_BOTTOM_WITH_FACE_DOWN_PILE) {
            // Jace, Architect of Thought −2: chosen pile → controller's hand; other pile → the bottom
            // of their library in an order they choose (an async LibraryReorder when two or more).
            boolean chosenPileHidden = state.disposition() == CardPileDisposition.HAND_AND_BOTTOM_WITH_FACE_DOWN_PILE
                    && accepted == state.controllerChoosesPile();
            for (UUID cardId : chosenPileCardIds) {
                Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
                if (card != null) {
                    gameData.addCardToHand(controllerId, card);
                    gameLogService.append(gameData, chosenPileHidden
                            ? GameLog.text(controllerName + " puts a face-down card into their hand.")
                            : GameLog.textCardText(controllerName + " puts ", card, " into their hand."));
                }
            }
            List<Card> toBottom = otherPileCardIds.stream()
                    .map(cardId -> allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (toBottom.isEmpty()) {
                return;
            }
            if (toBottom.size() == 1) {
                gameData.playerDecks.get(controllerId).add(toBottom.getFirst());
                boolean bottomPileHidden = state.disposition() == CardPileDisposition.HAND_AND_BOTTOM_WITH_FACE_DOWN_PILE
                        && !chosenPileHidden;
                gameLogService.append(gameData, bottomPileHidden
                        ? GameLog.text(controllerName + " puts a face-down card on the bottom of their library.")
                        : GameLog.textCardText(controllerName + " puts ", toBottom.getFirst(),
                        " on the bottom of their library."));
                return;
            }
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, toBottom, true, controllerId,
                    "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
            return;
        }

        if (state.disposition() == CardPileDisposition.ONE_FROM_CHOSEN_HAND_AND_BOTTOM) {
            List<Card> chosenPile = chosenPileCardIds.stream()
                    .map(cardId -> allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
            List<Card> otherCards = otherPileCardIds.stream()
                    .map(cardId -> allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (chosenPile.isEmpty()) {
                bottomTruthOrTaleCards(gameData, controllerId, otherCards);
                return;
            }
            gameData.queueInteraction(new PendingTruthOrTaleCardChoice(controllerId, chosenPile, otherCards));
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, chosenPile, 1, 1,
                    "Choose one card from the chosen pile to put into your hand.");
            return;
        }

        if (state.disposition() == CardPileDisposition.HAND_AND_EXILE_WITH_FACE_DOWN_PILE) {
            for (UUID cardId : chosenPileCardIds) {
                Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
                if (card != null && gameData.removeFromExile(cardId)) {
                    gameData.addCardToHand(controllerId, card);
                }
            }
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " puts " + chosenPileName + " into their hand and leaves the other pile exiled."));
            return;
        }

        if (state.disposition() == CardPileDisposition.HAND
                || state.disposition() == CardPileDisposition.HAND_FROM_EXILE
                || state.disposition() == CardPileDisposition.HAND_AND_THOPTER
                || state.disposition() == CardPileDisposition.HAND_WITH_FACE_DOWN_PILE) {
            // Fact-or-Fiction (Unesh): chosen pile → controller's hand; other pile → controller's graveyard.
            boolean chosenPileIsFaceDown = state.disposition() == CardPileDisposition.HAND_WITH_FACE_DOWN_PILE
                    && accepted == state.controllerChoosesPile();
            if (chosenPileIsFaceDown) {
                gameLogService.append(gameData, GameLog.text(
                        controllerName + " puts the face-down pile into their hand."));
            }
            for (UUID cardId : chosenPileCardIds) {
                Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
                if (card != null) {
                    gameData.addCardToHand(controllerId, card);
                    if (!chosenPileIsFaceDown) {
                        gameLogService.append(gameData, GameLog.textCardText(
                                controllerName + " puts ", card, " into their hand."));
                    }
                }
            }
            List<Card> graveyardPile = otherPileCardIds.stream()
                    .map(cardId -> allCards.stream().filter(card -> card.getId().equals(cardId))
                            .findFirst().orElse(null))
                    .filter(Objects::nonNull).toList();
            List<Card> enteredGraveyard;
            if (state.disposition() == CardPileDisposition.HAND_FROM_EXILE) {
                enteredGraveyard = new ArrayList<>();
                for (Card card : graveyardPile) {
                    if (graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.EXILE)) {
                        enteredGraveyard.add(card);
                    }
                }
            } else {
                enteredGraveyard = graveyardService.addCardsFromLibraryToGraveyard(
                        gameData, controllerId, graveyardPile);
            }
            int cardsPutIntoGraveyard = enteredGraveyard.size();
            for (Card card : enteredGraveyard) {
                gameLogService.append(gameData, GameLog.textCardText(controllerName + " puts ", card,
                        " into their graveyard."));
            }
            if (state.disposition() == CardPileDisposition.HAND_AND_THOPTER
                    && gameData.pendingEffectResolutionEntry != null) {
                createIntrudeThopter(gameData, gameData.pendingEffectResolutionEntry, cardsPutIntoGraveyard);
            }
            return;
        }

        if (state.disposition() == CardPileDisposition.SEARCH_ONE_TO_HAND) {
            completePortalPileSearch(gameData, controllerId, controllerName, allCards,
                    chosenPileCardIds, otherPileCardIds, cardOwners);
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<ReturnedPermanent> enteredPermanents = new ArrayList<>();
        for (UUID cardId : chosenPileCardIds) {
            Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (card != null) {
                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromExile(true);
                        battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                enteredPermanents.add(new ReturnedPermanent(controllerId, permanent, card));
                gameLogService.append(gameData, GameLog.textCardText(
                        controllerName + " puts ", card, " onto the battlefield."));
            }
        }
        for (ReturnedPermanent returned : enteredPermanents) {
            handleCreatureEtbAndLegendRule(gameData, returned.controllerId(), returned.permanent(), returned.card());
        }

        // Other pile → owners' graveyards
        for (UUID cardId : otherPileCardIds) {
            Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (card != null) {
                UUID ownerId = cardOwners.get(cardId);
                gameData.playerGraveyards.get(ownerId).add(card);
                String ownerName = gameData.playerIdToName.get(ownerId);
                gameLogService.append(gameData, GameLog.cardThen(card, " returns to " + ownerName + "'s graveyard."));
            }
        }
    }

    /**
     * Creates Intrude on the Mind's 0/0 Thopter, then puts a +1/+1 counter on it for each card put into
     * the graveyard. The counters arrive after the token has entered, so enter triggers see it as a 0/0.
     */
    public void createIntrudeThopter(GameData gameData, StackEntry entry, int counters) {
        List<UUID> before = new ArrayList<>(entry.getCreatedPermanentIds());
        createTokenEffectHandler.resolve(gameData, entry,
                new CreateTokenEffect(
                        CardType.CREATURE, 1, "Thopter", 0, 0, null, Set.of(),
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT),
                        false, false, Map.of(), List.of(), false, false, false,
                        0, Set.of(), Set.of()));
        if (counters <= 0) {
            return;
        }
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            if (before.contains(createdId)) {
                continue;
            }
            Permanent thopter = gameQueryService.findPermanentById(gameData, createdId);
            if (thopter != null) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, thopter, CounterType.PLUS_ONE_PLUS_ONE, counters);
            }
        }
    }

    private void completeAbstractPerformancePileChoice(GameData gameData, PendingPileSeparation state,
                                                        List<UUID> chosenPileCardIds,
                                                        List<UUID> otherPileCardIds, List<Card> allCards) {
        UUID controllerId = state.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        for (UUID cardId : chosenPileCardIds) {
            Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (card == null) {
                continue;
            }
            ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
            if (exiled != null && gameData.removeFromExile(card.getId())) {
                graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
            }
        }

        List<Card> otherCards = otherPileCardIds.stream()
                .map(cardId -> allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        List<Card> castableCards = otherCards.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .collect(Collectors.toCollection(ArrayList::new));
        if (otherCards.isEmpty() || castableCards.isEmpty()) {
            for (Card card : otherCards) {
                if (gameData.removeFromExile(card.getId())) {
                    gameData.addCardToHand(controllerId, card);
                }
            }
            return;
        }

        String prompt = "You may cast a spell from the other pile without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, castableCards)
                        .reveals(false)
                        .canFailToFind(true)
                        .sourceCards(otherCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_ONE_AND_PUT_REST_INTO_HAND)
                        .build(), prompt, true));
        gameLogService.append(gameData, GameLog.text(controllerName
                + " may cast a spell from the other pile without paying its mana cost."));
    }

    /** Completes Truth or Tale's one-card choice and starts the ordered bottoming step if needed. */
    public void completeTruthOrTaleCardChoice(GameData gameData, List<UUID> selectedCardIds) {
        PendingTruthOrTaleCardChoice state = gameData.pollPendingInteraction(PendingTruthOrTaleCardChoice.class);
        if (state == null || selectedCardIds.size() != 1) {
            throw new IllegalStateException("Truth or Tale requires exactly one card");
        }

        UUID selectedCardId = selectedCardIds.getFirst();
        Card selectedCard = state.chosenPileCards().stream()
                .filter(card -> card.getId().equals(selectedCardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid Truth or Tale card"));
        gameData.interaction.clearAwaitingInput();
        UUID controllerId = state.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        gameData.addCardToHand(controllerId, selectedCard);
        gameLogService.append(gameData, GameLog.textCardText(
                controllerName + " puts ", selectedCard, " into their hand."));

        List<Card> toBottom = new ArrayList<>(state.otherCards());
        state.chosenPileCards().stream()
                .filter(card -> !card.getId().equals(selectedCardId))
                .forEach(toBottom::add);
        bottomTruthOrTaleCards(gameData, controllerId, toBottom);
    }

    private void bottomTruthOrTaleCards(GameData gameData, UUID controllerId, List<Card> toBottom) {
        if (toBottom.isEmpty()) {
            return;
        }
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (toBottom.size() == 1) {
            gameData.playerDecks.get(controllerId).add(toBottom.getFirst());
            gameLogService.append(gameData, GameLog.textCardText(
                    controllerName + " puts ", toBottom.getFirst(), " on the bottom of their library."));
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, toBottom, true, controllerId,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
    }

    private void completeDeathOrGloryPileChoice(GameData gameData, PendingPileSeparation state,
                                                List<UUID> exilePileCardIds, List<UUID> battlefieldPileCardIds,
                                                List<Card> allCards, Map<UUID, UUID> cardOwners,
                                                boolean accepted) {
        UUID controllerId = state.controllerId();
        UUID chooserId = state.targetPlayerId();
        String chooserName = gameData.playerIdToName.get(chooserId);
        String exilePileName = accepted ? "Pile 1" : "Pile 2";
        gameLogService.append(gameData, GameLog.text(chooserName + " chooses " + exilePileName + " to exile."));

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Map<Permanent, UUID> controllersByPermanent = new LinkedHashMap<>();

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : exilePileCardIds) {
                Card card = findCard(allCards, cardId);
                if (card != null && exileCardFromAnyGraveyard(gameData, cardId, card)) {
                    gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
                }
            }

            for (UUID cardId : battlefieldPileCardIds) {
                Card card = findCard(allCards, cardId);
                UUID ownerId = cardOwners.get(cardId);
                if (card == null || ownerId == null
                        || gameQueryService.findCardInGraveyardById(gameData, cardId) == null) {
                    continue;
                }
                if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                    gameLogService.append(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(controllerId) + " can't return ", card,
                            " from the graveyard; it remains there."));
                    continue;
                }

                permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
                Permanent permanent = new Permanent(card);
                permanent.setEnteredFromGraveyardOwnerId(ownerId);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                controllersByPermanent.put(permanent, controllerId);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(controllerId) + " returns ", card,
                        " to the battlefield."));
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        for (Map.Entry<Permanent, UUID> permanentEntry : controllersByPermanent.entrySet()) {
            Permanent permanent = permanentEntry.getKey();
            handleCreatureEtbAndLegendRule(gameData, permanentEntry.getValue(), permanent, permanent.getCard());
        }
    }

    private Card findCard(List<Card> cards, UUID cardId) {
        return cards.stream().filter(card -> card.getId().equals(cardId)).findFirst().orElse(null);
    }

    /**
     * Gifts-style effects: the opponent has chosen which revealed cards go to the effect's selected
     * destination; every other card in the pool goes to the other configured destination. Unlike the
     * pile-separation dispositions this completes the flow — there is no second choice.
     */
    public void completeGiftsUngivenChoice(GameData gameData, List<UUID> chosenCardIds) {
        completeGiftsUngivenChoice(gameData, chosenCardIds, false);
    }

    /**
     * Completes a Gifts-style opponent choice. The chosen cards go to the controller's graveyard
     * for Gifts effects or back into their library for Threats Undetected; the remaining cards
     * either go to hand or enter the controller's battlefield tapped.
     */
    public void completeGiftsUngivenChoice(GameData gameData, List<UUID> chosenCardIds,
                                            boolean remainingCardsEnterBattlefieldTapped) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        UUID controllerId = state.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String chooserName = gameData.playerIdToName.get(state.targetPlayerId());
        boolean chosenCardsReturnToLibrary = state.disposition()
                == CardPileDisposition.THREATS_UNDETECTED;
        Set<CardType> enterTappedTypes = remainingCardsEnterBattlefieldTapped
                ? battlefieldEntryService.snapshotEnterTappedTypes(gameData) : Set.of();
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Permanent> enteredPermanents = new ArrayList<>();

        for (Card card : state.cards()) {
            if (chosenCardIds.contains(card.getId())) {
                if (chosenCardsReturnToLibrary) {
                    gameData.playerDecks.computeIfAbsent(controllerId, k -> new ArrayList<>()).add(card);
                    gameLogService.append(gameData, GameLog.textCardText(chooserName + " chooses ", card,
                            ", shuffling it into " + controllerName + "'s library."));
                } else if (graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY)) {
                    gameLogService.append(gameData, GameLog.textCardText(chooserName + " chooses ", card,
                            ", putting it into " + controllerName + "'s graveyard."));
                }
            } else if (remainingCardsEnterBattlefieldTapped) {
                if (isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
                    gameData.playerDecks.computeIfAbsent(controllerId, ignored -> new ArrayList<>()).add(card);
                    gameLogService.append(gameData, GameLog.cardThen(card,
                            " can't enter the battlefield from a library; it stays in the library."));
                    continue;
                }

                Permanent permanent = new Permanent(card, Zone.LIBRARY);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
                permanent.tap();
                simultaneouslyEntered.add(permanent);
                enteredPermanents.add(permanent);
                gameLogService.append(gameData, GameLog.entersBattlefieldTappedUnder(card, controllerName));
            } else {
                gameData.addCardToHand(controllerId, card);
                gameLogService.append(gameData, GameLog.textCardText(controllerName + " puts ", card,
                        " into their hand."));
            }
        }

        for (Permanent permanent : enteredPermanents) {
            handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, permanent.getCard());
        }
        if (chosenCardsReturnToLibrary) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(controllerName + "'s library is shuffled."));
        }
    }

    /**
     * Completes Deliver Unto Evil: the chosen cards stay in the controller's graveyard and every
     * other card in the held target set returns to that player's hand.
     */
    public void completeDeliverUntoEvilChoice(GameData gameData, List<UUID> chosenCardIds) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        UUID controllerId = state.controllerId();
        List<Card> returnedCards = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            if (graveyard != null) {
                for (Card card : state.cards()) {
                    if (!chosenCardIds.contains(card.getId())
                            && graveyard.removeIf(graveyardCard -> graveyardCard.getId().equals(card.getId()))) {
                        addCardToHandFromGraveyard(gameData, controllerId, controllerId, card);
                        graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
                        returnedCards.add(card);
                    }
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (!returnedCards.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            GameLog.Builder builder = GameLog.builder().text(playerName + " returns ");
            appendCardList(builder, returnedCards);
            builder.text(" from graveyard to hand.");
            gameLogService.append(gameData, builder.build());
        }
    }

    public Permanent putCardOntoBattlefieldFromExile(GameData gameData, UUID controllerId, Card card) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " puts " , card, " onto the battlefield."));

        handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
        return permanent;
    }

    /** Puts a card that was removed from exile onto the battlefield with {@code amount} counters of one kind. */
    public Permanent putCardOntoBattlefieldFromExileWithCounters(GameData gameData, UUID controllerId, Card card,
                                                                 CounterType counterType, int amount) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        if (counterType != null && amount > 0) {
            int counters = gameQueryService.replaceCounters(gameData, permanent, controllerId,
                    counterType, amount, controllerId);
            permanent.setCounterCount(counterType, counters);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " puts " , card, " onto the battlefield."));

        handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
        return permanent;
    }

    /**
     * Phyrexian Portal step 2: the unchosen pile is exiled, then the controller searches the chosen
     * pile for one card to put into their hand. The search is a {@code LibraryRevealChoice} marked
     * by {@link PendingPortalPileSearch}; {@code LibraryChoiceHandlerService} shuffles the pile's
     * leftovers back into the library once the pick lands. An empty chosen pile has nothing to
     * search, but the library is still shuffled as the ability instructs.
     */
    private void completePortalPileSearch(GameData gameData, UUID controllerId, String controllerName,
                                          List<Card> allCards, List<UUID> chosenPileCardIds,
                                          List<UUID> otherPileCardIds, Map<UUID, UUID> cardOwners) {
        for (UUID cardId : otherPileCardIds) {
            Card card = allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (card != null) {
                exileService.exileCard(gameData, cardOwners.get(cardId), card);
                gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
            }
        }

        List<Card> chosenPile = new ArrayList<>();
        for (UUID cardId : chosenPileCardIds) {
            allCards.stream().filter(c -> c.getId().equals(cardId)).findFirst().ifPresent(chosenPile::add);
        }
        if (chosenPile.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " has no cards to search."));
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        gameData.queueInteraction(new PendingPortalPileSearch(controllerId));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, chosenPile, chosenPile.stream().map(Card::getId).toList(),
                false, true, false, false, false, 0, null, 1,
                "Search this pile for a card to put into your hand. "
                        + "The rest of the pile is shuffled into your library.", 1, false));
    }

    private String describePileSize(List<UUID> cardIds) {
        return cardIds.size() == 1 ? "1 card" : cardIds.size() + " cards";
    }

    private String buildCardPileDescription(List<Card> allCards, List<UUID> cardIds) {
        if (cardIds.isEmpty()) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (UUID cardId : cardIds) {
            allCards.stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .ifPresent(c -> names.add(c.getName()));
        }
        return String.join(", ", names);
    }

    private void appendCardPile(GameLog.Builder builder, List<Card> allCards, List<UUID> cardIds) {
        if (cardIds.isEmpty()) {
            builder.text("empty");
            return;
        }
        List<Card> pileCards = new ArrayList<>();
        for (UUID cardId : cardIds) {
            allCards.stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .ifPresent(pileCards::add);
        }
        appendCardList(builder, pileCards);
    }

    /**
     * A graveyard-activated ability that moves its own source out of the graveyard keeps referring to
     * that card: later effects of the same ability must not treat the exit it caused as the card
     * becoming a different object.
     */
    private static void keepOwnAbilityBoundToMovedSource(GameData gameData, StackEntry entry, Card movedCard) {
        if (entry != null && entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && entry.getCard() != null && entry.getCard().getId().equals(movedCard.getId())) {
            entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(movedCard.getId()));
        }
    }

    /**
     * Captures the targeted graveyard card's power and toughness before it moves, so a following
     * effect reads the values it had as it last existed in the graveyard (CR 608.2h) rather than
     * those it has in its new zone (a characteristic-defining value such as Maro's counts the hand).
     */
    public void recordTargetPowerToughnessBeforeReturn(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> candidates = new java.util.ArrayList<>();
        List<UUID> bound = entry.targetsForBoundEffectGroup(effect);
        if (bound != null) {
            candidates.addAll(bound);
        }
        if (entry.getTargetCardIds() != null) {
            candidates.addAll(entry.getTargetCardIds());
        }
        if (entry.getTargetId() != null) {
            candidates.add(entry.getTargetId());
        }
        for (UUID candidateId : candidates) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, candidateId);
            if (card != null) {
                entry.setReturnedGraveyardCardId(candidateId);
                entry.setReturnedGraveyardCardPowerAtLastKnown(gameQueryService.getEffectiveCardPower(gameData, card));
                entry.setReturnedGraveyardCardToughnessAtLastKnown(
                        gameQueryService.getEffectiveCardToughness(gameData, card));
                return;
            }
        }
    }
}
