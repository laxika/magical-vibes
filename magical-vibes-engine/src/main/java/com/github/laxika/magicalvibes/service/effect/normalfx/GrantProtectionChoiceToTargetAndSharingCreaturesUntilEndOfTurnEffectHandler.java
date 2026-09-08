package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceToTargetAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantProtectionChoiceToTargetAndSharingCreaturesUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionChoiceToTargetAndSharingCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        Set<CardColor> targetColors = gameQueryService.getEffectiveColors(gameData, target);
        List<UUID> affectedIds = new ArrayList<>();
        affectedIds.add(target.getId());
        gameData.forEachPermanent((ignored, permanent) -> {
            if (permanent.getId().equals(target.getId())
                    || !gameQueryService.isCreature(gameData, permanent)) {
                return;
            }
            if (targetColors.stream().anyMatch(gameQueryService.getEffectiveColors(gameData, permanent)::contains)) {
                affectedIds.add(permanent.getId());
            }
        });

        playerInputService.beginProtectionColorChoice(gameData, entry.getControllerId(), affectedIds, false);
    }
}
