package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersWithCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealSubtypeOrEntersWithCountersHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealSubtypeOrEntersWithCountersEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealSubtypeOrEntersWithCountersEffect revealOrCounters = ability.effects().stream()
                .filter(e -> e instanceof RevealSubtypeOrEntersWithCountersEffect)
                .map(RevealSubtypeOrEntersWithCountersEffect.class::cast)
                .findFirst().orElse(null);
        if (revealOrCounters == null) {
            return;
        }

        Permanent source = ability.sourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (accepted && source != null) {
            int placed = permanentCounterSupport.placeCounterOnPermanent(gameData, null, source,
                    revealOrCounters.counterType(), revealOrCounters.counterCount());
            if (revealOrCounters.counterType() == CounterType.PLUS_ONE_PLUS_ONE
                    && placed > 0) {
                permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                        gameData, source, placed, player.getId());
            }
            List<Card> hand = gameData.playerHands.get(ability.controllerId());
            Card revealed = hand == null ? null : hand.stream()
                    .filter(card -> card.getSubtypes().stream().anyMatch(revealOrCounters.subtypes()::contains))
                    .findFirst().orElse(null);
            String revealedName = revealed == null ? revealOrCounters.subtypes().stream()
                    .map(subtype -> subtype.getDisplayName()).findFirst().orElse("matching") : revealed.getName();
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " reveals " + revealedName
                    + " — ", ability.sourceCard(), " enters with a counter."));
            log.info("Game {} - {} reveals {} to have {} enter with counters", gameData.id,
                    player.getUsername(), revealedName, ability.sourceCard().getName());
        } else if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to reveal for ",
                    ability.sourceCard(), "."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
