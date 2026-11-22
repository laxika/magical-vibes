package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnAllCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final AuraAttachmentService auraAttachmentService;
    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolve(gameData, entry, (ReturnAllCardsExiledWithSourceEffect) effect, null);
    }

    void returnAllCardsExiledWithSourceExcept(GameData gameData, StackEntry entry,
                                              UUID excludedCardId) {
        resolve(gameData, entry, new ReturnAllCardsExiledWithSourceEffect(), excludedCardId);
    }

    private void resolve(GameData gameData, StackEntry entry,
                         ReturnAllCardsExiledWithSourceEffect returnEffect,
                         UUID excludedCardId) {
        List<ExiledCardEntry> toReturn = returnEffect.cards() != null
                ? namedCardsInExile(gameData, entry, returnEffect)
                : cardsExiledWithSource(gameData, entry, returnEffect, excludedCardId);
        if (toReturn != null) {
            returnCards(gameData, entry, returnEffect, toReturn);
        }
    }

    /** The cards the effect's card subject names that are in exile, each once. */
    private List<ExiledCardEntry> namedCardsInExile(GameData gameData, StackEntry entry,
                                                    ReturnAllCardsExiledWithSourceEffect returnEffect) {
        List<ExiledCardEntry> named = new ArrayList<>();
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, returnEffect, returnEffect.cards())) {
            ExiledCardEntry exiled = located.zone() == Zone.EXILE
                    ? gameData.findExiledCard(located.card().getId()) : null;
            if (exiled != null && !named.contains(exiled)) {
                named.add(exiled);
            }
        }
        return named;
    }

    /** The cards exiled with the source (or its linked permanent) the effect returns; {@code null} with no source. */
    private List<ExiledCardEntry> cardsExiledWithSource(GameData gameData, StackEntry entry,
                                                        ReturnAllCardsExiledWithSourceEffect returnEffect,
                                                        UUID excludedCardId) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (returnEffect.useLinkedSource()) {
            // The cards exiled with the permanent the source is linked to, read from the source as it last existed.
            Permanent linkingSource = entry.getSourcePermanentSnapshot();
            sourcePermanentId = linkingSource == null ? null : linkingSource.getChosenPermanentId();
        } else if (sourcePermanentId == null && entry.getCard() != null) {
            sourcePermanentId = entry.getCard().getId();
        }
        if (sourcePermanentId == null) {
            return null;
        }
        UUID sourceId = sourcePermanentId;

        if (returnEffect.turnFaceUp()) {
            for (int i = 0; i < gameData.exiledCards.size(); i++) {
                ExiledCardEntry exiledEntry = gameData.exiledCards.get(i);
                if (sourcePermanentId.equals(exiledEntry.sourcePermanentId())
                        && exiledEntry.faceDown()) {
                    gameData.exiledCards.set(i, new ExiledCardEntry(exiledEntry.card(),
                            exiledEntry.ownerId(), exiledEntry.sourcePermanentId(), false,
                            exiledEntry.exilerId(), exiledEntry.exiledTurnNumber(),
                            exiledEntry.controllerTurnsTakenAtExile(), exiledEntry.abilityLink()));
                }
            }
        }

        return gameData.exiledCards.stream()
                .filter(e -> sourceId.equals(e.sourcePermanentId()))
                .filter(e -> excludedCardId == null
                        || !excludedCardId.equals(e.card().getId()))
                .filter(e -> returnEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        e.card(), returnEffect.filter(), entry.getCard().getId()))
                .toList();
    }

    private void returnCards(GameData gameData, StackEntry entry, ReturnAllCardsExiledWithSourceEffect returnEffect,
                             List<ExiledCardEntry> toReturn) {
        boolean underControllerControl = returnEffect.underControllerControl();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<SimultaneousEntry> enteredCreatures = new ArrayList<>();
        entry.clearReturnedPermanentIds();

        // CR 303.4f/303.4g: an Aura returning here is attached as its entering player chooses, and
        // the choice is made from the objects already on the battlefield before this batch enters.
        // Only one such Aura is chosen per resolution; an Aura with no legal host stays in exile.
        ExiledCardEntry auraAwaitingHost = null;
        List<UUID> auraHostCandidates = List.of();
        List<ExiledCardEntry> nonAuraReturns = new ArrayList<>();
        for (ExiledCardEntry exiledEntry : toReturn) {
            Card card = exiledEntry.card();
            if (!card.isAura() || card.hasType(CardType.CREATURE)) {
                nonAuraReturns.add(exiledEntry);
                continue;
            }
            UUID auraControllerId = underControllerControl ? entry.getControllerId() : exiledEntry.ownerId();
            List<UUID> hosts = legalAuraHosts(gameData, card, auraControllerId);
            if (auraAwaitingHost == null && !hosts.isEmpty()) {
                auraAwaitingHost = exiledEntry;
                auraHostCandidates = hosts;
            }
        }

        for (ExiledCardEntry exiledEntry : nonAuraReturns) {
            Card card = exiledEntry.card();
            UUID newControllerId = underControllerControl ? entry.getControllerId() : exiledEntry.ownerId();
            if (!returnEffect.enterFaceDown() && gameQueryService.cantEnterFaceUp(card)) {
                continue;
            }
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }

            Permanent perm = new Permanent(card);
            perm.setEnteredFromExile(true);
            if (returnEffect.enterTapped()) {
                perm.tap();
            }
            if (!returnEffect.grantedKeywords().isEmpty()) {
                gameData.addEffectOnPermanent(entry,
                        new GrantKeywordEffect(Set.copyOf(returnEffect.grantedKeywords()), Subjects.target()),
                        perm.getId(), Duration.PERMANENT);
            }
            if (returnEffect.enterFaceDown()) {
                Set<CardSubtype> subtypes = returnEffect.grantedSubtype() == null
                        ? Set.of() : Set.of(returnEffect.grantedSubtype());
                perm.setFaceDown(2, 2, Set.of(CardType.ARTIFACT, CardType.CREATURE), subtypes);
            } else {
                applyPermanentCharacteristics(gameData, entry, newControllerId, perm, returnEffect);
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, newControllerId, perm,
                    enterTappedTypes, simultaneouslyEntered);
            if (exiledEntry.ownerId() != null && !exiledEntry.ownerId().equals(newControllerId)) {
                // Gisa: the card still belongs to its owner, so it goes to that player's graveyard.
                graveyardReturnSupport.trackStolenCreature(gameData, perm.getId(), newControllerId,
                        exiledEntry.ownerId());
            }
            entry.rememberReturnedPermanent(perm.getId());
            simultaneouslyEntered.add(perm);
            if (returnEffect.sacrificeAtEndStep()) {
                DelayedPermanentActions.schedule(gameData, entry,
                        new DelayedPermanentAction(perm.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP),
                        ActivePlayer.ANY);
            }

            gameLogService.append(gameData, returnEffect.cards() != null
                    ? GameLog.textCardText(gameData.playerIdToName.get(newControllerId) + " puts ", card,
                    " onto the battlefield.")
                    : GameLog.builder().card(card).text(" returns to the battlefield under "
                    + gameData.playerIdToName.get(newControllerId) + "'s control.").build());
            log.info("Game {} - {} returns from exile via {} (put into graveyard from battlefield)",
                    gameData.id, card.getName(), entry.getCard().getName());
            enteredCreatures.add(new SimultaneousEntry(card, newControllerId));
        }

        // CR 603.6a: every permanent in one batch is on the battlefield before enters-the-battlefield
        // triggers are checked, so each newcomer sees the others (e.g. Purphoros and a creature it sees
        // enter from the same pile).
        for (SimultaneousEntry enteredCreature : enteredCreatures) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, enteredCreature.controllerId(),
                    enteredCreature.card(), null, false);
        }

        if (auraAwaitingHost != null) {
            beginAuraReturnChoice(gameData, entry, auraAwaitingHost, auraHostCandidates, underControllerControl);
        }
    }

    private void beginAuraReturnChoice(GameData gameData, StackEntry entry, ExiledCardEntry auraEntry,
                                       List<UUID> hostCandidates, boolean underControllerControl) {
        UUID chooserId = underControllerControl ? entry.getControllerId() : auraEntry.ownerId();
        beginAuraReturnChoice(gameData, chooserId, auraEntry, hostCandidates);
    }

    /** Removes the Aura from exile and has {@code chooserId} choose the object it enchants (CR 303.4f). */
    void beginAuraReturnChoice(GameData gameData, UUID chooserId, ExiledCardEntry auraEntry,
                               List<UUID> hostCandidates) {
        Card auraCard = auraEntry.card();
        if (!gameData.removeFromExile(auraCard.getId())) {
            return;
        }
        gameData.interaction.setPendingAuraCard(auraCard);
        List<UUID> permanentTargets = hostCandidates.stream()
                .filter(id -> !gameData.playerIds.contains(id))
                .toList();
        List<UUID> playerTargets = hostCandidates.stream()
                .filter(gameData.playerIds::contains)
                .toList();
        if (playerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, chooserId, permanentTargets,
                    "Choose a permanent for " + auraCard.getName() + " to enchant.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, chooserId, permanentTargets, playerTargets,
                    "Choose a permanent or player for " + auraCard.getName() + " to enchant.");
        }
    }

    List<UUID> legalAuraHosts(GameData gameData, Card auraCard, UUID auraControllerId) {
        List<UUID> hosts = new ArrayList<>();
        gameData.forEachPermanent((ignored, candidate) -> {
            if (auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, candidate)
                    && !gameQueryService.cantBeChosen(gameData, candidate)) {
                hosts.add(candidate.getId());
            }
        });
        if (auraCard.isEnchantPlayer()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (auraAttachmentService.canEnchantPlayer(gameData, auraCard, auraControllerId, playerId)) {
                    hosts.add(playerId);
                }
            }
        }
        return hosts;
    }

    private void applyPermanentCharacteristics(GameData gameData, StackEntry entry, UUID controllerId,
                                               Permanent permanent,
                                               ReturnAllCardsExiledWithSourceEffect effect) {
        if (effect.basePower() != null || effect.baseToughness() != null) {
            gameData.addEffectOnPermanent(entry, new SetBasePowerToughnessEffect(effect.basePower(),
                    effect.baseToughness(), Subjects.target(), Duration.PERMANENT), permanent.getId(), Duration.PERMANENT);
        }
        if (effect.grantedSubtype() != null) {
            gameData.addEffectOnPermanent(entry, new GrantSubtypeEffect(effect.grantedSubtype(), Subjects.target()),
                    permanent.getId(), Duration.PERMANENT);
        }
        if (effect.enteringCounterType() != null
                && !gameQueryService.cantHaveCounters(gameData, permanent)) {
            int placed = gameQueryService.replaceCounters(gameData, permanent, controllerId,
                    effect.enteringCounterType(), 1);
            if (placed > 0) {
                permanent.setCounterCount(effect.enteringCounterType(),
                        permanent.getCounterCount(effect.enteringCounterType()) + placed);
            }
        }
    }

    /** A card returned by this batch, with the player it entered under, kept until the whole batch is on the battlefield. */
    private record SimultaneousEntry(Card card, UUID controllerId) {
    }
}
