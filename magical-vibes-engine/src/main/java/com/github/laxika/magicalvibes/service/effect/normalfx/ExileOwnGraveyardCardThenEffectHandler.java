package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an optional graveyard exile and queues its reflexive follow-up ability. */
@Component
@RequiredArgsConstructor
public class ExileOwnGraveyardCardThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;
    private final PlayerInputService playerInputService;
    @Autowired
    @Lazy
    private EffectResolutionService effectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnGraveyardCardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileOwnGraveyardCardThenEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade) {
            UUID chosenCardId = gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade = false;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardId == null) {
                return;
            }

            Card chosen = findMatchingCard(gameData, entry, exileThen, chosenCardId);
            if (chosen != null) {
                exileCard(gameData, entry, chosen, exileThen);
                if (exileThen.resolvesInline()) {
                    resolveInline(gameData, entry, exileThen.thenEffect());
                } else {
                    queueReflexiveAbility(gameData, entry, exileThen.thenEffect());
                }
            }
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, exileThen);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no matching card in its controller's graveyard to exile."));
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeExileThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, candidates, 1,
                exileThen.mandatory() ? 1 : 0,
                entry.getCard().getName() + " — " + (exileThen.mandatory()
                        ? "Choose a matching card from your graveyard."
                        : "You may exile a matching card from your graveyard."));
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
                                     ExileOwnGraveyardCardThenEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.exileFilter(), sourceCardId))
                .toList();
    }

    private Card findMatchingCard(GameData gameData, StackEntry entry,
                                  ExileOwnGraveyardCardThenEffect effect, UUID cardId) {
        return matchingCards(gameData, entry, effect).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void exileCard(GameData gameData, StackEntry entry, Card card,
                           ExileOwnGraveyardCardThenEffect exileThen) {
        UUID controllerId = entry.getControllerId();
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        if (exileThen.trackWithSource() && entry.getSourcePermanentId() != null) {
            exileService.exileCard(gameData, controllerId, card, entry.getSourcePermanentId());
        } else {
            exileService.exileCard(gameData, controllerId, card);
        }
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getCard().getName() + " exiles ", card,
                        " from its controller's graveyard."));
    }

    private void resolveInline(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        StackEntry inlineAbility = StackEntry.carrier(entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(thenEffect)),
                thenEffect.targetSpec() == TargetSpec.NONE ? null : entry.getTargetId(),
                entry.getSourcePermanentId()
        );
        inlineAbility.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        inlineAbility.setNonTargeting(true);
        effectResolutionService.resolveEffects(gameData, inlineAbility);
    }

    /** "When you do": a reflexive ability whose targets are chosen at the stack door as it goes on the stack (CR 603.12). */
    private void queueReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        queueReflexiveAbilityEffectHandler.resolve(gameData, entry, new QueueReflexiveAbilityEffect(thenEffect));
    }

}
