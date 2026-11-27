package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.effect.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastMatchingCardsFromGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastMatchingCardsFromGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AllowCastMatchingCardsFromGraveyardThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> owners = subjectResolutionService.players(gameData, entry, effect, e.graveyards());
        if (owners.isEmpty()) {
            return;
        }
        boolean anyGraveyard = owners.size() > 1;
        if (anyGraveyard && !owners.containsAll(gameData.orderedPlayerIds)) {
            throw new IllegalStateException("A graveyard cast grant names one graveyard or every graveyard");
        }
        UUID graveyardOwnerId = anyGraveyard ? null : owners.getFirst();

        // Recorded as a filter, not per card: cards that reach the graveyard later this turn are
        // covered too. Turn cleanup drops the grant. The grant covers only the graveyards it names ("from your
        // graveyard", "from that player's graveyard", "from any graveyard").
        gameData.addPlayerEffect(entry, new GameData.GraveyardCastFilterPermission(controllerId, e.filter(),
                        e.singleUse(), e.entryCounterType(), e.grantedSubtype(), anyGraveyard,
                        e.exileInsteadOfGraveyard(), e.additionalCost(), e.enterWithCounter(),
                        e.enterWithCounterCount(), graveyardOwnerId, e.manaAsThoughAnyColor(),
                        e.manaAsThoughAnyColor()),
                controllerId, Duration.UNTIL_END_OF_TURN);

        String graveyard = anyGraveyard ? "any graveyard"
                : controllerId.equals(graveyardOwnerId) ? "their graveyard"
                : gameData.playerIdToName.get(graveyardOwnerId) + "'s graveyard";
        String spells = !anyGraveyard && !controllerId.equals(graveyardOwnerId) && e.singleUse()
                ? "a matching spell" : "matching spells";
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may cast " + spells + " from " + graveyard + " this turn."
                + (e.exileInsteadOfGraveyard() ? " They are exiled instead." : "")));
        log.info("Game {} - {} may cast spells matching {} from {} this turn",
                gameData.id, gameData.playerIdToName.get(controllerId), e.filter(), graveyard);
    }
}
