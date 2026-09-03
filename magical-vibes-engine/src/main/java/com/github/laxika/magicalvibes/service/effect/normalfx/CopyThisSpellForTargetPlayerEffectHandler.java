package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyThisSpellForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyThisSpellForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId != null && !gameData.playerIds.contains(targetPlayerId)) {
            targetPlayerId = gameQueryService.findPermanentController(gameData, targetPlayerId);
        }
        if (targetPlayerId == null) {
            return;
        }

        Card spellCard = entry.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        Card copyCard = copySupport.createCopyCard(spellCard);
        StackEntry copyEntry = copySupport.createCopyStackEntry(entry, copyCard, targetPlayerId, targetPlayerId);
        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
        log.info("Game {} - copy of {} created for target player", gameData.id, spellCard.getName());

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                spellCard,
                targetPlayerId,
                List.of(new CopySpellEffect()),
                "Choose new targets for the copy of " + spellCard.getName() + "?",
                copyCard.getId()
        ));
    }
}
