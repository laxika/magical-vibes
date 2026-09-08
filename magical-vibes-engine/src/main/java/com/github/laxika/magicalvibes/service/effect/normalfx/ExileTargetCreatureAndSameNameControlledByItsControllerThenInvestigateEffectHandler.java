package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PermanentControlSupport permanentControlSupport;
    private final TriggerCollectionService triggerCollectionService;

    public ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffectHandler(
            GameQueryService gameQueryService,
            GameLogService gameLogService,
            PermanentRemovalService permanentRemovalService,
            PermanentControlSupport permanentControlSupport,
            @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.permanentRemovalService = permanentRemovalService;
        this.permanentControlSupport = permanentControlSupport;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return;
        }

        String targetName = target.getCard().getName();
        List<Permanent> toExile = collectSameNameCreatures(gameData, controllerId, targetName);
        int nontokenCount = (int) toExile.stream().filter(permanent -> !permanent.getCard().isToken()).count();

        for (Permanent permanent : toExile) {
            permanentRemovalService.removePermanentToExile(gameData, permanent);
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (nontokenCount > 0) {
            permanentControlSupport.applyCreateToken(gameData, controllerId,
                    CreateTokenEffect.ofClueToken(nontokenCount), entry.getCard().getSetCode());
            triggerCollectionService.checkInvestigateTriggers(gameData, controllerId);
        }
    }

    private List<Permanent> collectSameNameCreatures(GameData gameData, UUID controllerId,
                                                       String targetName) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }

        List<Permanent> matches = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && permanent.getCard().getName().equals(targetName)) {
                matches.add(permanent);
            }
        }
        return List.copyOf(matches);
    }
}
