package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCopiedSpellForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyCopiedSpellForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final PsychicBattleSupport psychicBattleSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyCopiedSpellForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CopyCopiedSpellForTargetPlayerEffect) effect;
        StackEntry spellSnapshot = copyEffect.spellSnapshot();
        UUID targetPlayerId = entry.getTargetId();
        if (spellSnapshot == null || targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        Card spellCard = spellSnapshot.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        Card copyCard = copySupport.createCopyCard(spellCard);
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                spellSnapshot, copyCard, targetPlayerId, spellSnapshot.getTargetId());
        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.builder()
                .text("A copy of ").card(spellCard).text(" is created for ")
                .text(gameData.playerIdToName.get(targetPlayerId)).text(".").build());
        log.info("Game {} - copy of {} created for target player", gameData.id, spellCard.getName());

        if (copyEntry.getTargetId() != null) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    targetPlayerId,
                    List.of(new CopySpellEffect()),
                    "Choose new targets for the copy of " + spellCard.getName() + "?",
                    copyCard.getId()));
        } else if (copyEntry.getTargetIds().size() == 1) {
            psychicBattleSupport.queueNextChoice(gameData, entry.getCard(), targetPlayerId, copyCard.getId(), 0);
        }
    }
}
