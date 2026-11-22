package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect.AllFromGraveyard;
import com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect.AnyNumberYouControl;
import com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect.FaceDown;
import com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect.PermanentsAndTopCards;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link ExileFaceDownPileEffect}: exiles the pile face down, shuffles it and puts it onto the battlefield
 * face down as one entry. A pile of chosen permanents pauses at its end on the choice.
 */
@Component
@RequiredArgsConstructor
public class ExileFaceDownPileEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileFaceDownPileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileFaceDownPileEffect) effect;
        entry.recordStep(StepKind.MANIFEST, StepOutcome.NONE);
        UUID controllerId = entry.getControllerId();
        switch (e.pile()) {
            case PermanentsAndTopCards pile -> {
                List<Card> cards = exilePermanents(gameData,
                        subjectResolutionService.permanents(gameData, entry, e, pile.permanents()));
                List<Card> library = gameData.playerDecks.get(controllerId);
                for (int i = 0; i < pile.topCards() && library != null && !library.isEmpty(); i++) {
                    Card top = library.removeFirst();
                    exileService.exileCardFaceDown(gameData, controllerId, top, null);
                    cards.add(top);
                }
                putFaceDown(gameData, entry, e, cards);
            }
            case AllFromGraveyard pile -> {
                List<Card> cards = new ArrayList<>();
                for (UUID playerId : subjectResolutionService.players(gameData, entry, e, pile.player())) {
                    List<Card> exiled = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                            .filter(card -> !card.isToken())
                            .filter(card -> predicateEvaluationService.matchesCardPredicate(
                                    card, pile.filter(), entry.getCard().getId(), gameData, playerId))
                            .toList();
                    permanentRemovalService.removeCardsFromGraveyardForExile(gameData, playerId, exiled);
                    exiled.forEach(card -> exileService.exileCardFaceDown(gameData, playerId, card, null));
                    cards.addAll(exiled);
                }
                putFaceDown(gameData, entry, e, cards);
            }
            case AnyNumberYouControl pile -> {
                List<UUID> options = options(gameData, entry, pile);
                if (options.isEmpty()) {
                    return;
                }
                playerInputService.beginMultiPermanentChoice(gameData, controllerId, options, options.size(),
                        new MultiPermanentChoiceContext.ExileInFaceDownPile(entry, e),
                        entry.getCard().getName() + " — Choose any number of " + pile.label() + " you control.");
            }
        }
    }

    /** Whether each of {@code permanentIds} may still be chosen for the pile. */
    public boolean canChoose(GameData gameData, List<UUID> permanentIds,
                             MultiPermanentChoiceContext.ExileInFaceDownPile context) {
        return options(gameData, context.resolvingEntry(), (AnyNumberYouControl) context.effect().pile())
                .containsAll(permanentIds);
    }

    /** The answer to a pile of chosen permanents. */
    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.ExileInFaceDownPile context) {
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                chosen.add(permanent);
            }
        }
        // The entry that resumes is the one a later step reads.
        StackEntry entry = gameData.pendingEffectResolutionEntry != null
                ? gameData.pendingEffectResolutionEntry : context.resolvingEntry();
        putFaceDown(gameData, entry, context.effect(), exilePermanents(gameData, chosen));
    }

    private List<UUID> options(GameData gameData, StackEntry entry, AnyNumberYouControl pile) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        return gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, pile.filter(), filterContext))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    /** Exiles the permanents face down as one event (CR 603.10a) and returns the cards they became; a token none. */
    private List<Card> exilePermanents(GameData gameData, List<Permanent> permanents) {
        List<Card> cards = new ArrayList<>();
        if (permanents.isEmpty()) {
            return cards;
        }
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : permanents) {
                List<Card> leaving = List.copyOf(permanent.cardsLeavingBattlefield());
                if (!permanentRemovalService.removePermanentToExileFaceDown(gameData, permanent)) {
                    continue;
                }
                for (Card card : leaving) {
                    ExiledCardEntry exiled = card.isToken() ? null : gameData.findExiledCard(card.getId());
                    if (exiled != null) {
                        cards.add(exiled.card());
                    }
                }
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        return cards;
    }

    private void putFaceDown(GameData gameData, StackEntry entry, ExileFaceDownPileEffect effect, List<Card> pile) {
        if (pile.isEmpty()) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        List<Card> shuffled = new ArrayList<>(pile);
        Collections.shuffle(shuffled);
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> entered = new ArrayList<>();
        List<Card> enteredCards = new ArrayList<>();
        for (Card card : shuffled) {
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }
            Permanent permanent = new Permanent(card);
            if (effect.turned() == FaceDown.CLOAK) {
                permanent.setFaceDownAsCloaked();
            } else {
                permanent.setManifested(true);
                permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
            }
            if (effect.tapped()) {
                permanent.tap();
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, entered);
            entered.add(permanent);
            enteredCards.add(card);
        }
        if (entered.isEmpty()) {
            return;
        }
        entry.recordStep(StepKind.MANIFEST, StepOutcome.ofObjects(
                entered.stream().map(Permanent::getId).toList(), List.of(controllerId)));
        for (Card card : enteredCards) {
            battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, card);
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), effect.turned() == FaceDown.CLOAK
                ? " exiles a face-down pile and cloaks " + entered.size() + " card(s)."
                : " exiles a face-down pile and manifests " + entered.size() + " card(s)."));
    }
}
