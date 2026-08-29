package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayReturnPermanentToHandAndEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the optional as-enters choice for returning a permanent. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayReturnPermanentToHandAndEnterWithCountersHandler implements MayEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayReturnPermanentToHandAndEnterWithCountersEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var choice = ability.effects().stream()
                .filter(MayReturnPermanentToHandAndEnterWithCountersEffect.class::isInstance)
                .map(MayReturnPermanentToHandAndEnterWithCountersEffect.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Return-and-counter choice is missing its effect"));

        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines the return choice for ", ability.sourceCard(), "."));
            resumeEntry(gameData, ability);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(ability.sourceCard().getId())
                .withSourceControllerId(ability.controllerId())
                .withSourcePermanentId(ability.sourcePermanentId());
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachBattlefield((controllerId, battlefield) -> battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, choice.filter(), filterContext))
                .map(Permanent::getId)
                .forEach(validIds::add));

        if (validIds.isEmpty()) {
            resumeEntry(gameData, ability);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayReturnPermanentToHandAndEnterWithCounters(
                ability.sourceCard(), ability.controllerId(), choice, ability.sourcePermanentId(),
                ability.targetCardId()));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validIds,
                ability.sourceCard().getName() + " — Choose " + choice.permanentDescription()
                        + " to return to its owner's hand.");
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " accepts the return choice for ", ability.sourceCard(), "."));
        log.info("Game {} - {} is choosing a permanent for {}", gameData.id, player.getUsername(),
                ability.sourceCard().getName());
    }

    private void resumeEntry(GameData gameData, PendingMayAbility ability) {
        Permanent source = ability.sourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        boolean wasCastFromHand = source != null && source.isCast() && source.getCastFromZone() == Zone.HAND;
        battlefieldEntryService.processCreatureETBEffects(
                gameData, ability.controllerId(), ability.sourceCard(), ability.targetCardId(), wasCastFromHand);
    }
}
