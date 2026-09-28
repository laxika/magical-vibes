package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadChosenCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Grenzo's goad mode by choosing a creature controlled by the damaged player. */
@Component
@RequiredArgsConstructor
public class GoadChosenCreatureDamagedPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TargetLegalityService targetLegalityService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadChosenCreatureDamagedPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null) {
            return;
        }

        List<UUID> validIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(damagedPlayerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && targetLegalityService.checkTriggeredPermanentTargetableReason(
                    gameData, permanent, entry.getCard(), entry.getControllerId()).isEmpty()) {
                validIds.add(permanent.getId());
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability resolves, but there are no legal creatures to goad."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), validIds, 1,
                new MultiPermanentChoiceContext.GoadDamagedPlayerControls(
                        entry.getCard().getName(), entry.getControllerId()),
                entry.getCard().getName() + "'s ability — Choose a creature "
                        + gameData.playerIdToName.get(damagedPlayerId) + " controls to goad.");
    }
}
