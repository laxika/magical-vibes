package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getCard() != null) {
            sourcePermanentId = entry.getCard().getId();
        }
        if (sourcePermanentId == null) {
            return;
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

        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(e -> sourceId.equals(e.sourcePermanentId()))
                .filter(e -> excludedCardId == null
                        || !excludedCardId.equals(e.card().getId()))
                .filter(e -> returnEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        e.card(), returnEffect.filter(), entry.getCard().getId()))
                .toList();

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
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }

            Permanent perm = new Permanent(card);
            perm.setEnteredFromExile(true);
            if (returnEffect.enterTapped()) {
                perm.tap();
            }
            perm.getPersistentGrantedKeywords().addAll(returnEffect.grantedKeywords());
            if (returnEffect.enterFaceDown()) {
                Set<CardSubtype> subtypes = returnEffect.grantedSubtype() == null
                        ? Set.of() : Set.of(returnEffect.grantedSubtype());
                perm.setFaceDown(2, 2, Set.of(CardType.ARTIFACT, CardType.CREATURE), subtypes);
            } else {
                applyPermanentCharacteristics(gameData, newControllerId, perm, returnEffect);
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
                gameData.queueDelayedAction(new DelayedPermanentAction(perm.getId(),
                        DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
            }

            gameLogService.append(gameData, GameLog.builder().card(card).text(" returns to the battlefield under " + gameData.playerIdToName.get(newControllerId) + "'s control.").build());
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
            if (auraAttachmentService.canEnchant(gameData, auraCard, auraControllerId, candidate)) {
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

    private void applyPermanentCharacteristics(GameData gameData, UUID controllerId,
                                               Permanent permanent,
                                               ReturnAllCardsExiledWithSourceEffect effect) {
        if (effect.basePower() != null) {
            permanent.setBasePowerOverriddenPermanently(true);
            permanent.setPermanentBasePowerOverride(effect.basePower());
            permanent.setPermanentBasePowerOverrideTimestamp(gameData.nextTimestamp());
        }
        if (effect.baseToughness() != null) {
            permanent.setBaseToughnessOverriddenPermanently(true);
            permanent.setPermanentBaseToughnessOverride(effect.baseToughness());
            permanent.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());
        }
        if (effect.grantedSubtype() != null
                && !permanent.getGrantedSubtypes().contains(effect.grantedSubtype())) {
            permanent.getGrantedSubtypes().add(effect.grantedSubtype());
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
