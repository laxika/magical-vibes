package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopySpellEffect copyEffect = (CopySpellEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Copy target no longer on stack", gameData.id);
            return;
        }

        // CR 706.2 — "This spell can't be copied": the copy is simply not created.
        if (targetEntry.getCard().isCantBeCopied()) {
            gameBroadcastService.logAndBroadcast(gameData, targetEntry.getCard().getName() + " can't be copied.");
            log.info("Game {} - {} can't be copied", gameData.id, targetEntry.getCard().getName());
            return;
        }

        UUID copyControllerId = entry.getControllerId();
        Card copyCard = copySupport.createCopyCard(targetEntry.getCard());
        // Creature-copy mode (Choreographed Sparks): the copy is a token that gains haste and is
        // sacrificed at the next end step. Haste rides on the token's own keywords; the sacrifice is
        // registered when the token enters the battlefield (see BattlefieldEntryService).
        if (copyEffect.tokenWithHaste()) {
            copyCard.setToken(true);
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            if (copyCard.getKeywords() != null) {
                keywords.addAll(copyCard.getKeywords());
            }
            keywords.add(Keyword.HASTE);
            copyCard.setKeywords(keywords);
            copyCard.setSacrificeAtEndStep(copyEffect.sacrificeAtEndStep());
        }
        StackEntry copyEntry = copySupport.createCopyStackEntry(targetEntry, copyCard, copyControllerId, targetEntry.getTargetId());

        gameData.stack.add(copyEntry);

        String logMsg = "A copy of " + targetEntry.getCard().getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} copies {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());

        // Only the instant/sorcery-copy mode offers "you may choose new targets for the copy".
        if (!copyEffect.tokenWithHaste() && copyEntry.getTargetId() != null) {
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    copyControllerId,
                    List.of(new CopySpellEffect()),
                    "Choose new targets for the copy of " + targetEntry.getCard().getName() + "?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }
}
