package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainControlOfTargetSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetSpellId = entry.getTargetId();
        if (targetSpellId == null) {
            return;
        }

        StackEntry targetSpell = gameData.stack.stream()
                .filter(stackEntry -> targetSpellId.equals(stackEntry.getCard().getId()))
                .findFirst()
                .orElse(null);
        if (targetSpell == null) {
            return;
        }

        UUID spellOwnerId = targetSpell.getOwnerId();
        targetSpell.setOwnerIdOverride(spellOwnerId);
        targetSpell.setControllerId(entry.getControllerId());

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" gains control of ")
                .card(targetSpell.getCard())
                .text(".")
                .build());

        if (hasSpellTargets(targetSpell)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(new ChooseNewTargetsForTargetSpellEffect()),
                    "Choose new targets for " + targetSpell.getCard().getName() + "?",
                    targetSpell.getCard().getId()));
        }
    }

    private boolean hasSpellTargets(StackEntry spellEntry) {
        return spellEntry.getTargetId() != null
                || !spellEntry.getDeclaredTargetIds().isEmpty()
                || !spellEntry.getTargetCardIds().isEmpty();
    }
}
