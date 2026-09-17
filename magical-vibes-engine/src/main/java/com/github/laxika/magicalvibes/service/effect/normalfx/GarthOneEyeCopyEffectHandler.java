package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GarthOneEyeCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithNormalCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Creates the selected Garth One-Eye copy and offers it for its normal mana cost. */
@Component
@RequiredArgsConstructor
public class GarthOneEyeCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GarthOneEyeCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GarthOneEyeCopyEffect garthEffect = (GarthOneEyeCopyEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            if (!source.getChosenModeLabels().add(garthEffect.card().getName())) {
                return;
            }
        }

        Card copy = copySupport.createCopyCard(garthEffect.card());
        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                entry.getControllerId(),
                List.of(new MayCastCopyWithNormalCostEffect()),
                "Cast the copy of " + copy.getName() + " by paying its mana cost?",
                copy.getId()));
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " creates a copy of ",
                garthEffect.card(), "."));
    }
}
