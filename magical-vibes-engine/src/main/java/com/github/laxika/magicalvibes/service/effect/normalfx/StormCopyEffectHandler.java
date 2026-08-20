package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.StormCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StormCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return StormCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (StormCopyEffect) effect;
        if (e.spellSnapshot() == null || e.copies() <= 0) return;

        StackEntry spellSnapshot = e.spellSnapshot();
        UUID castingPlayerId = e.castingPlayerId();
        Card spellCard = spellSnapshot.getCard();

        // CR 707.10 — a spell that "can't be copied" is not copied.
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        for (int i = 0; i < e.copies(); i++) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            if (e.tokenCopy()) {
                copyCard.setToken(true);
            }
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    spellSnapshot, copyCard, castingPlayerId, spellSnapshot.getTargetId());

            copySupport.addCopyToStack(gameData, copyEntry);

            gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));

            if (!e.tokenCopy() && copyEntry.getTargetId() != null) {
                PendingMayAbility retargetAbility = new PendingMayAbility(
                        entry.getCard(),
                        castingPlayerId,
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + spellCard.getName() + "?",
                        copyCard.getId()
                );
                gameData.pendingMayAbilities.addFirst(retargetAbility);
            }
        }

        log.info("Game {} - spell-copy effect creates {} copies of {} for {}",
                gameData.id, e.copies(), spellCard.getName(), castingPlayerId);
    }
}
