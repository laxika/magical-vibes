package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherAttackingCreatureWithLesserPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseAnotherAttackingCreatureWithLesserPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GrantKeywordEffectHandler grantKeywordEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseAnotherAttackingCreatureWithLesserPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = firstTargetId(entry, effect);
        Permanent target = targetId == null
                ? null
                : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target) || !target.isAttacking()) {
            return;
        }

        int targetPower = gameQueryService.getEffectivePower(gameData, target);
        List<UUID> validIds = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of())
                .stream()
                .filter(permanent -> !permanent.getId().equals(targetId))
                .filter(permanent -> permanent.isAttacking())
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> gameQueryService.getEffectivePower(gameData, permanent) < targetPower)
                .map(Permanent::getId)
                .toList();

        if (validIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                validIds,
                1,
                new MultiPermanentChoiceContext.ChooseAnotherAttackingCreatureWithLesserPower(),
                "Choose another attacking creature with lesser power.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds, StackEntry entry) {
        if (permanentIds.size() != 1) {
            return;
        }
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
        if (chosen == null) {
            return;
        }
        grantKeywordEffectHandler.grantToPermanent(gameData, entry, chosen, Set.of(Keyword.DOUBLE_STRIKE));
    }

    private UUID firstTargetId(StackEntry entry, CardEffect effect) {
        return entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
    }
}
