package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayHaveThisLandEnterTappedForRadCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MayHaveThisLandEnterTappedForRadCountersHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayHaveThisLandEnterTappedForRadCountersEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayHaveThisLandEnterTappedForRadCountersEffect effect = ability.effects().stream()
                .filter(MayHaveThisLandEnterTappedForRadCountersEffect.class::isInstance)
                .map(MayHaveThisLandEnterTappedForRadCountersEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted) {
            Permanent source = ability.sourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            if (source != null) {
                source.tap();
            }
            gameData.playerRadCounters.merge(ability.controllerId(), effect.radCounterCount(), Integer::sum);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " gets " + effect.radCounterCount() + " rad counters — ",
                    ability.sourceCard(), " enters tapped."));
            log.info("Game {} - {} has {} enter tapped and gets {} rad counters",
                    gameData.id, player.getUsername(), ability.sourceCard().getName(), effect.radCounterCount());
        }

        if (ability.sourceCard().hasType(CardType.LAND)) {
            battlefieldEntryService.processLandETBEffects(gameData, ability.controllerId(), ability.sourceCard());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
