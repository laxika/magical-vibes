package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mutation = (ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect) effect;
        if (mutation.controllersToReveal() != null) {
            List<UUID> controllers = mutation.controllersToReveal();
            if (controllers.isEmpty()) {
                return;
            }
            if (controllers.size() > 1) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                        new ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect(
                                controllers.subList(1, controllers.size()))));
            }
            StackEntry revealEntry = new StackEntry(entry);
            revealEntry.setControllerId(controllers.getFirst());
            revealHandler.resolve(gameData, revealEntry, new RevealUntilCardPredicateRestOnBottomRandomEffect(
                    new CardTypePredicate(CardType.CREATURE), LibrarySearchDestination.BATTLEFIELD));
            return;
        }
        List<UUID> targetIds = entry.targetsForEffect(effect);
        List<ExiledTarget> exiledTargets = new ArrayList<>();

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (UUID targetId : targetIds) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }

                UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
                if (targetControllerId == null
                        || !permanentRemovalService.removePermanentToExile(gameData, target)) {
                    continue;
                }

                exiledTargets.add(new ExiledTarget(targetControllerId));
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        if (exiledTargets.isEmpty()) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        List<UUID> controllerOrder = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = controllerOrder.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            java.util.Collections.rotate(controllerOrder, -activeIndex);
        }
        exiledTargets.sort(java.util.Comparator.comparingInt(
                target -> controllerOrder.indexOf(target.controllerId())));
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                new ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect(
                        exiledTargets.stream().map(ExiledTarget::controllerId).toList())));
    }

    private record ExiledTarget(UUID controllerId) {
    }
}
