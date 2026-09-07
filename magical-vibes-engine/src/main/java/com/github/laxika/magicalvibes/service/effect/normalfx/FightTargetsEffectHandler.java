package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FightTargetsEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final GameQueryService gameQueryService;
    private final TargetLegalityService targetLegalityService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FightTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FightTargetsEffect) effect;

        boolean hasCapturedTargets = e.secondTargetId() != null;
        List<UUID> firstGroup = hasCapturedTargets
                ? e.firstTargetId() == null ? List.of() : List.of(e.firstTargetId())
                : entry.targetsForGroup(e.firstTargetGroup());
        List<UUID> secondGroup = hasCapturedTargets
                ? List.of(e.secondTargetId())
                : entry.targetsForGroup(e.secondTargetGroup());
        if (firstGroup.isEmpty() || secondGroup.isEmpty()) {
            return; // Optional target not chosen ("up to one") — no fight happens
        }

        if (hasCapturedTargets && !areCapturedTargetsLegal(gameData, entry,
                firstGroup.getFirst(), secondGroup.getFirst())) {
            return;
        }

        fightSupport.fight(gameData, entry,
                gameQueryService.findPermanentById(gameData, firstGroup.getFirst()),
                gameQueryService.findPermanentById(gameData, secondGroup.getFirst()));
    }

    private boolean areCapturedTargetsLegal(GameData gameData, StackEntry entry,
                                            UUID firstTargetId, UUID secondTargetId) {
        var first = gameQueryService.findPermanentById(gameData, firstTargetId);
        var second = gameQueryService.findPermanentById(gameData, secondTargetId);
        if (first == null || second == null
                || !gameQueryService.isCreature(gameData, first)
                || !gameQueryService.isCreature(gameData, second)) {
            return false;
        }

        UUID firstController = gameQueryService.findPermanentController(gameData, firstTargetId);
        UUID secondController = gameQueryService.findPermanentController(gameData, secondTargetId);
        if (!entry.getControllerId().equals(firstController)
                || entry.getControllerId().equals(secondController)) {
            return false;
        }
        return targetLegalityService.checkTriggeredPermanentTargetableReason(
                gameData, first, entry.getCard(), entry.getControllerId()).isEmpty()
                && targetLegalityService.checkTriggeredPermanentTargetableReason(
                gameData, second, entry.getCard(), entry.getControllerId()).isEmpty()
                && !gameQueryService.cantBeTargetOfOpponentAbilities(gameData, second);
    }
}
