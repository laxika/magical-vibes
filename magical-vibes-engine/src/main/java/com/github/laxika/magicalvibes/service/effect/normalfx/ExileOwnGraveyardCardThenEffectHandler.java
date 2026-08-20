package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves an optional graveyard exile and queues its reflexive follow-up ability. */
@Component
@RequiredArgsConstructor
public class ExileOwnGraveyardCardThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

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
                exileCard(gameData, entry, chosen);
                queueReflexiveAbility(gameData, entry, exileThen.thenEffect());
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
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, candidates, 1, 0,
                entry.getCard().getName() + " — You may exile a matching card from your graveyard.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
                                     ExileOwnGraveyardCardThenEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> !card.getId().equals(sourceCardId))
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

    private void exileCard(GameData gameData, StackEntry entry, Card card) {
        UUID controllerId = entry.getControllerId();
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        exileService.exileCard(gameData, controllerId, card);
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getCard().getName() + " exiles ", card,
                        " from its controller's graveyard."));
    }

    private void queueReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        List<Card> targets = matchingReturnTargets(gameData, entry, thenEffect);
        if (targets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s reflexive ability has no legal targets."));
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (targets.size() == 1) {
            Card target = targets.getFirst();
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    entry.getControllerId(),
                    entry.getCard().getName() + "'s reflexive ability",
                    new ArrayList<>(List.of(thenEffect)),
                    0,
                    target.getId(),
                    sourcePermanentId,
                    Map.of(),
                    Zone.GRAVEYARD,
                    null,
                    null
            ));
            gameLogService.append(gameData,
                    GameLog.builder().card(entry.getCard()).text("'s reflexive ability targets ")
                            .card(target).text(" in its graveyard.").build());
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (targets.contains(graveyard.get(i))) {
                matchingIndices.add(i);
            }
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(entry.getControllerId(), matchingIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        entry.getCard().getName() + "'s reflexive ability — Choose an instant or sorcery card from your graveyard to return to your hand.")
                .mayAbilityContext(entry.getCard(), entry.getControllerId(), List.of(thenEffect), sourcePermanentId)
                .build());
    }

    private List<Card> matchingReturnTargets(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        if (!(thenEffect instanceof ReturnCardFromGraveyardEffect returnEffect)) {
            return List.of();
        }
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, returnEffect.filter(), sourceCardId))
                .toList();
    }
}
