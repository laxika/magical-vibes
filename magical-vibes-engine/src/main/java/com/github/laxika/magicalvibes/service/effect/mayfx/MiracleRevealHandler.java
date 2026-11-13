package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MiracleMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.MiracleRevealEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Draw-time miracle reveal choice (CR 702.94a). Accepting reveals the card and puts the linked
 * miracle triggered ability onto the stack.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MiracleRevealHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MiracleRevealEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var card = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " declines to reveal ", card, " for miracle."));
            log.info("Game {} - {} declines miracle reveal for {}", gameData.id, playerName, card.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(card)
                .text(" for its miracle ability.")
                .build());
        log.info("Game {} - {} reveals {} for miracle", gameData.id, playerName, card.getName());

        MiracleRevealEffect reveal = ability.effects().stream()
                .filter(MiracleRevealEffect.class::isInstance)
                .map(MiracleRevealEffect.class::cast)
                .findFirst().orElse(new MiracleRevealEffect());
        // "When you reveal this card this way, you may cast it ..." is the miracle card's own triggered ability, linked to
        // its static ability (CR 702.94a): the door puts it on the stack.
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                new TriggeredAbility(Triggers.youRevealCardThisWay(),
                        new MiracleMayCastEffect(reveal.miracleCost(), reveal.xReduction())),
                card, null, player.getId(), card.getName() + "'s miracle",
                ConditionContext.forCard(card, player.getId()), TriggeredAbilityRequest.TriggerRecord.NONE));
        gameData.priorityPassedBy.clear();

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
