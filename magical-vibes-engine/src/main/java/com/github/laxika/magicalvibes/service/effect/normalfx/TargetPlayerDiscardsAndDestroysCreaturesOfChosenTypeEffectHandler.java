package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsAndDestroysCreaturesOfChosenTypeEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves the chosen-type hand discard and the targeted battlefield destruction in order. The
 * subtype choice is shared by both halves and is cleared after it has been consumed.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsAndDestroysCreaturesOfChosenTypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final CardRevealService cardRevealService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsAndDestroysCreaturesOfChosenTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        discardMatchingCreatureCards(gameData, entry, targetPlayerId, chosenSubtype);
        destroyMatchingCreatures(gameData, entry, targetPlayerId, chosenSubtype);
    }

    private void discardMatchingCreatureCards(GameData gameData, StackEntry entry, UUID targetPlayerId,
                                               CardSubtype chosenSubtype) {
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
            return;
        }

        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
        Set<CardSubtype> grantedSubtypes = new HashSet<>(
                gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, targetPlayerId));
        List<Card> matchingCards = hand.stream()
                .filter(card -> isMatchingCreatureCard(card, chosenSubtype, grantedSubtypes))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        hand.removeAll(matchingCards);
        boolean discardCausedByOpponent = !targetPlayerId.equals(entry.getControllerId());
        gameData.discardCausedByOpponent = discardCausedByOpponent;
        List<Card> normallyDiscarded = new ArrayList<>();
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        for (Card card : matchingCards) {
            if (discardCausedByOpponent && hasEnterBattlefieldOnDiscardEffect(card)) {
                Permanent permanent = new Permanent(card);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent);
                gameLogService.append(gameData, GameLog.textCardText(
                        targetName + " discards ", card, " — it enters the battlefield instead."));
                if (card.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetPlayerId, card, null, false);
                }
            } else {
                graveyardService.discardCard(gameData, targetPlayerId, card);
                normallyDiscarded.add(card);
            }
            triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
        }

        if (!normallyDiscarded.isEmpty()) {
            GameLog.Builder discardLog = GameLog.builder().text(targetName + " discards ");
            for (int i = 0; i < normallyDiscarded.size(); i++) {
                if (i > 0) {
                    discardLog.text(", ");
                }
                discardLog.card(normallyDiscarded.get(i));
            }
            gameLogService.append(gameData, discardLog.text(".").build());
        }
    }

    private boolean isMatchingCreatureCard(Card card, CardSubtype chosenSubtype, Set<CardSubtype> grantedSubtypes) {
        return card.hasType(CardType.CREATURE)
                && (card.getSubtypes().contains(chosenSubtype)
                || card.getKeywords().contains(Keyword.CHANGELING)
                || grantedSubtypes.contains(chosenSubtype));
    }

    private boolean hasEnterBattlefieldOnDiscardEffect(Card card) {
        return card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                .anyMatch(EnterBattlefieldOnDiscardEffect.class::isInstance);
    }

    private void destroyMatchingCreatures(GameData gameData, StackEntry entry, UUID targetPlayerId,
                                           CardSubtype chosenSubtype) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        PermanentPredicate filter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(chosenSubtype)));
        List<Permanent> toDestroy = battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, filter, filterContext))
                .toList();
        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), true);
    }
}
