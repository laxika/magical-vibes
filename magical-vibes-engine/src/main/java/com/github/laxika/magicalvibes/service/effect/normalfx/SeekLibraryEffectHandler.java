package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves the digital Seek action without opening a player-choice search interaction, and records the
 * {@link StepKind#SEEK} step: the sought cards in the zone each went to, the permanents they became on the battlefield,
 * and the seekers.
 */
@Component
@RequiredArgsConstructor
public class SeekLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GraveyardService graveyardService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekLibraryEffect seek = (SeekLibraryEffect) effect;
        entry.recordStep(StepKind.SEEK, StepOutcome.NONE);
        Permanent currentSource = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent source = currentSource != null ? currentSource : entry.getSourcePermanentSnapshot();
        List<UUID> seekers = seek.seeker() == null ? List.of(entry.getControllerId())
                : subjectResolutionService.players(gameData, entry, seek, seek.seeker());
        AmountContext amountContext = AmountContext.forStackEntry(entry, source);
        // "A card that shares a card type with the exiled card", "a card with greater mana value than the discarded
        // card": read as the seek applies (CR 608.2h).
        CardPredicate filter = amountEvaluationService.bindAmounts(gameData,
                subjectResolutionService.bindCardSubjects(gameData, entry, seek, seek.filter()), amountContext);
        List<UUID> objectIds = new ArrayList<>();
        List<MovedCard> movedCards = new ArrayList<>();
        for (UUID seekerId : seekers) {
            seekFor(gameData, entry, seek, seekerId, source, filter, amountContext, objectIds, movedCards);
        }
        entry.recordStep(StepKind.SEEK, objectIds.isEmpty() ? StepOutcome.NONE
                : StepOutcome.ofObjects(objectIds, seekers, movedCards));
    }

    private void seekFor(GameData gameData, StackEntry entry, SeekLibraryEffect seek, UUID controllerId,
                         Permanent source, CardPredicate filter, AmountContext amountContext,
                         List<UUID> objectIds, List<MovedCard> movedCards) {
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Card sourceCard = source == null ? entry.getTargetingCard() : source.getCard();
        List<AllowCastFromCardsExiledWithSourceEffect> persistentPermissions = sourceCard == null
                ? List.of()
                : sourceCard.getStaticEffects().stream()
                .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                .filter(AllowCastFromCardsExiledWithSourceEffect::persistsAfterSourceLeaves)
                .toList();
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, seek.count(), amountContext));
        Integer manaValueBound = seek.manaValueBound() == null ? null
                : amountEvaluationService.evaluate(gameData, seek.manaValueBound().amount(), amountContext)
                + seek.manaValueBound().offset();
        if (deck == null || deck.isEmpty() || count == 0) {
            if (count > 0 && seek.destination() != LibrarySearchDestination.HAND) {
                logNoMatch(gameData, entry);
            }
            return;
        }

        UUID sourcePermanentId = seek.destination() == LibrarySearchDestination.EXILE_WITH_SOURCE
                ? resolveSourcePermanentId(gameData, entry, controllerId) : null;
        if (seek.destination() == LibrarySearchDestination.EXILE_WITH_SOURCE
                && sourcePermanentId == null && persistentPermissions.isEmpty()) {
            return;
        }
        boolean entersBattlefield = seek.destination() == LibrarySearchDestination.BATTLEFIELD
                || seek.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        // "From among the top ten cards of your library".
        List<Card> candidates = seek.fromAmongTop() == null ? deck
                : deck.subList(0, Math.clamp(amountEvaluationService.evaluate(gameData, seek.fromAmongTop(),
                amountContext), 0, deck.size()));

        List<Card> matchingCards = new ArrayList<>(candidates.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, entry.getCard() == null ? null : entry.getCard().getId(),
                        gameData, entry.getControllerId(), entry.getSourcePermanentId(), null, null, source)
                        && (manaValueBound == null || (seek.manaValueBound().exact()
                        ? card.getManaValue() == manaValueBound
                        : card.getManaValue() <= manaValueBound))
                        // A sought card is put into the hand, then onto the battlefield from there: a "can't enter
                        // from libraries" rule (Grafdigger's Cage) doesn't stop it.
                        && (!entersBattlefield
                        || !gameQueryService.isCardBlockedFromEnteringFromZone(
                                gameData, card, Zone.HAND)))
                .toList());

        int cardsToSeek = Math.min(count, matchingCards.size());
        if (cardsToSeek == 0 && seek.destination() != LibrarySearchDestination.HAND) {
            logNoMatch(gameData, entry);
        }
        List<Card> soughtCards = new ArrayList<>(cardsToSeek);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Card> simultaneousCreatureEntries = new ArrayList<>();
        String playerName = gameData.playerIdToName.get(controllerId);
        for (int i = 0; i < cardsToSeek; i++) {
            Card chosen = matchingCards.remove(ThreadLocalRandom.current().nextInt(matchingCards.size()));
            deck.removeIf(card -> card.getId().equals(chosen.getId()));
            if (seek.destination() == LibrarySearchDestination.HAND) {
                gameData.addCardToHand(controllerId, chosen);
                objectIds.add(chosen.getId());
                movedCards.add(gameData.boundToStay(new MovedCard(chosen.getId(), chosen.getId(), Zone.HAND,
                        controllerId)));
            } else if (entersBattlefield) {
                // Seek puts the card into the hand first and then onto the battlefield, so hand-from-library triggers see it.
                gameData.addCardToHand(controllerId, chosen);
                gameData.playerHands.get(controllerId).removeIf(card -> card.getId().equals(chosen.getId()));
                Permanent permanent = new Permanent(chosen, Zone.LIBRARY);
                if (seek.battlefieldCloaked()) {
                    permanent.setFaceDownAsCloaked();
                }
                if (seek.grantSubtype() != null) {
                    gameData.addEffectOnPermanent(entry, new GrantSubtypeEffect(seek.grantSubtype(), Subjects.target()),
                            permanent.getId(), Duration.PERMANENT);
                }
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent,
                        battlefieldEntryService.snapshotEnterTappedTypes(gameData), simultaneouslyEntered);
                simultaneouslyEntered.add(permanent);
                if (seek.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED) {
                    permanent.tap();
                }
                if (seek.battlefieldCloaked()) {
                    battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, chosen);
                } else if (chosen.hasType(CardType.LAND)) {
                    // A land's as-enters choices and enter abilities (Dryad Arbor's included).
                    battlefieldEntryService.processLandETBEffects(gameData, controllerId, chosen);
                } else {
                    // Every other permanent's as-enters choices and enter abilities, a noncreature one's too.
                    simultaneousCreatureEntries.add(chosen);
                }
                if (gameQueryService.findPermanentById(gameData, permanent.getId()) != null) {
                    objectIds.add(permanent.getId());
                }
                gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                        .text(seek.battlefieldCloaked()
                                ? "seeks and cloaks a card onto the battlefield."
                                : "seeks and puts " + chosen.getName() + " onto the battlefield.").build());
            } else if (seek.destination() == LibrarySearchDestination.GRAVEYARD) {
                // "Seek two cards with flashback and put them into your graveyard": from the hand a seek puts them in;
                // a random library card that isn't sought goes there from the library.
                Zone from = seek.triggersSeekAbilities() ? Zone.HAND : Zone.LIBRARY;
                if (from == Zone.HAND) {
                    gameData.addCardToHand(controllerId, chosen);
                    gameData.playerHands.get(controllerId).removeIf(card -> card.getId().equals(chosen.getId()));
                }
                boolean inGraveyard = graveyardService.addCardToGraveyard(gameData, controllerId, chosen, from);
                objectIds.add(chosen.getId());
                if (inGraveyard) {
                    movedCards.add(gameData.boundToStay(new MovedCard(chosen.getId(), chosen.getId(),
                            Zone.GRAVEYARD, controllerId)));
                    gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", chosen,
                            " into their graveyard."));
                } else if (gameData.findExiledCard(chosen.getId()) != null) {
                    // A replacement exiled it instead (Rest in Peace).
                    movedCards.add(gameData.boundToStay(new MovedCard(chosen.getId(), chosen.getId(), Zone.EXILE,
                            controllerId)));
                }
            } else if (seek.destination() == LibrarySearchDestination.EXILE
                    || seek.destination() == LibrarySearchDestination.EXILE_WITH_SOURCE) {
                // "Seek …, then exile them": the card is put into the hand first, so hand-from-library triggers see
                // it, and then exiled from there.
                gameData.addCardToHand(controllerId, chosen);
                gameData.playerHands.get(controllerId).removeIf(card -> card.getId().equals(chosen.getId()));
                if (seek.faceDown()) {
                    exileService.exileCardFaceDown(gameData, controllerId, chosen, sourcePermanentId);
                    gameData.grantExileLookPermission(chosen.getId(), controllerId);
                } else if (sourcePermanentId == null) {
                    exileService.exileCard(gameData, controllerId, chosen);
                } else {
                    exileService.exileCard(gameData, controllerId, chosen, sourcePermanentId);
                }
                if (seek.grantPlayUntilNextTurn()) {
                    exileSupport.grantPlayUntilOwnersNextTurn(gameData, chosen.getId(), controllerId);
                }
                grantPersistentPermissions(gameData, chosen, persistentPermissions, controllerId);
                objectIds.add(chosen.getId());
                movedCards.add(gameData.boundToStay(new MovedCard(chosen.getId(), chosen.getId(), Zone.EXILE,
                        controllerId)));
                if (seek.destination() == LibrarySearchDestination.EXILE) {
                    gameLogService.append(gameData, GameLog.textCardText(playerName + " seeks and exiles ", chosen,
                            seek.grantPlayUntilNextTurn() ? "; it may be played until the end of their next turn."
                                    : "."));
                }
            } else {
                throw new IllegalStateException("Unsupported Seek destination: " + seek.destination());
            }
            soughtCards.add(chosen);
        }
        if (!simultaneousCreatureEntries.isEmpty()) {
            simultaneouslyEntered.forEach(permanent -> gameData.permanentsEnteringSimultaneously.add(permanent.getId()));
            try {
                for (Card creature : simultaneousCreatureEntries) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, creature, null, false);
                }
            } finally {
                simultaneouslyEntered.forEach(permanent -> gameData.permanentsEnteringSimultaneously.remove(permanent.getId()));
            }
        }
        if (seek.destination() == LibrarySearchDestination.HAND && !soughtCards.isEmpty()) {
            // A sought card goes into a hand unrevealed (CR 400.2): the line counts, it doesn't name.
            gameLogService.append(gameData, GameLog.text(playerName + " seeks " + soughtCards.size() + " card"
                    + (soughtCards.size() == 1 ? "" : "s") + "."));
        }
        if (seek.destination() == LibrarySearchDestination.EXILE_WITH_SOURCE) {
            registerOneShotPermissionGroup(gameData, soughtCards, persistentPermissions);
        }
        if (!soughtCards.isEmpty() && seek.triggersSeekAbilities()) {
            ActionEvents.sought(gameData, controllerId, soughtCards);
        }
    }

    private void logNoMatch(GameData gameData, StackEntry entry) {
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks but finds no matching card.").build());
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry, UUID controllerId) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null && entry.getCard() != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }

    private void grantPersistentPermissions(GameData gameData, Card card,
                                            List<AllowCastFromCardsExiledWithSourceEffect> permissions,
                                            UUID exilerId) {
        for (AllowCastFromCardsExiledWithSourceEffect permission : permissions) {
            if (permission.filter() != null
                    && !predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null,
                    gameData, exilerId)) {
                continue;
            }
            UUID permittedPlayer = permission.accessScope() == ExileAccessScope.ACTIVE_PLAYER
                    ? gameData.activePlayerId : exilerId;
            gameData.grantExilePlayPermission(card.getId(), permittedPlayer, Duration.PERMANENT);
            if (permission.anyManaType()) {
                gameData.updateExilePlayPermission(card.getId(),
                        granted -> granted.withManaSpending(permission.manaSpending()));
            }
            break;
        }
    }

    private void registerOneShotPermissionGroup(
            GameData gameData, List<Card> soughtCards,
            List<AllowCastFromCardsExiledWithSourceEffect> permissions) {
        if (soughtCards.isEmpty() || permissions.stream().noneMatch(
                AllowCastFromCardsExiledWithSourceEffect::oneShot)) {
            return;
        }
        gameData.registerExilePlayPermissionGroup(UUID.randomUUID(), 1,
                soughtCards.stream().map(Card::getId).toList());
    }
}
