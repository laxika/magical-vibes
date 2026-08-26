package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the optional graveyard selection that collects evidence. */
@Component
@RequiredArgsConstructor
public class CollectEvidenceEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CollectEvidenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CollectEvidenceEffect collectEvidence = (CollectEvidenceEffect) effect;
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (collectEvidence.minimumManaValue() > 0
                && (graveyard == null || graveyard.isEmpty()
                || graveyard.stream().mapToInt(Card::getManaValue).sum() < collectEvidence.minimumManaValue())) {
            return;
        }

        if (collectEvidence.minimumManaValue() == 0 && (graveyard == null || graveyard.isEmpty())) {
            entry.setEventValue(0);
            triggerCollectionService.checkCollectEvidenceTriggers(gameData, entry.getControllerId());
            queueReflexiveAbility(gameData, entry, collectEvidence.thenEffect());
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeCollectEvidenceResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoiceWithMinimumManaValue(
                gameData, entry.getControllerId(), new ArrayList<>(graveyard), graveyard.size(),
                collectEvidence.minimumManaValue(),
                entry.getCard().getName() + " — You may collect evidence "
                        + collectEvidence.minimumManaValue() + ". Choose cards from your graveyard.");
    }

    public void queueReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        if (thenEffect == null) {
            return;
        }

        TargetSpec targetSpec = thenEffect.targetSpec();
        if (targetSpec.selfTargeting()) {
            StackEntry reflexiveAbility = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    entry.getControllerId(),
                    entry.getCard().getName() + "'s reflexive ability",
                    new ArrayList<>(List.of(thenEffect)),
                    entry.getXValue(),
                    entry.getSourcePermanentId());
            reflexiveAbility.setEventValue(entry.getEventValue());
            reflexiveAbility.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
            gameData.stack.add(reflexiveAbility);
            return;
        }
        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        List<UUID> validPermanentTargets = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard().getId())
                    .withSourceControllerId(entry.getControllerId());
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (targetPredicateEvaluationService.matchesPermanent(targetPredicate, permanent, filterContext)) {
                        validPermanentTargets.add(permanent.getId());
                    }
                }
            }
        }

        List<UUID> validPlayerTargets = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                ? gameData.orderedPlayerIds.stream()
                        .filter(playerId -> targetPredicateEvaluationService.matchesPlayer(
                                targetPredicate, playerId, entry.getControllerId(), gameData))
                        .toList()
                : List.of();
        if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(thenEffect)));
        if (validPlayerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    entry.getCard().getName() + "'s reflexive ability - Choose target.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    validPlayerTargets, entry.getCard().getName() + "'s reflexive ability - Choose target.");
        }
    }
}
