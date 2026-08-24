package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.EnumSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopySpellEffect copyEffect = (CopySpellEffect) effect;
        UUID targetCardId = entry.getTriggeringCardId() != null
                ? entry.getTriggeringCardId() : entry.getTargetId();
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

        // CR 707.10 — "This spell can't be copied": the copy is simply not created.
        if (targetEntry.getCard().isCantBeCopied()) {
            gameLogService.append(gameData, GameLog.cardThen(targetEntry.getCard(), " can't be copied."));
            log.info("Game {} - {} can't be copied", gameData.id, targetEntry.getCard().getName());
            return;
        }

        UUID copyControllerId = copyEffect.copyForTargetController()
                ? targetEntry.getControllerId()
                : entry.getControllerId();
        Card copyCard = copySupport.createCopyCard(targetEntry.getCard());
        if (copyEffect.removeLegendary()) {
            var supertypes = EnumSet.noneOf(CardSupertype.class);
            supertypes.addAll(copyCard.getSupertypes());
            supertypes.remove(CardSupertype.LEGENDARY);
            copyCard.setSupertypes(supertypes);
        }
        if (copyEffect.colorOverride() != null) {
            copyCard.setColor(copyEffect.colorOverride());
            copyCard.setColors(List.of(copyEffect.colorOverride()));
        }
        // Token-copy modes mark the copy before it resolves into a permanent. The creature-copy
        // mode additionally grants haste and may register a delayed sacrifice.
        if (copyEffect.tokenCopy() || copyEffect.tokenWithHaste()) {
            copyCard.setToken(true);
        }
        if (copyEffect.tokenWithHaste()) {
            copyCard.setSacrificeAtEndStep(copyEffect.sacrificeAtEndStep());
        }
        StackEntry copyEntry = copySupport.createCopyStackEntry(targetEntry, copyCard, copyControllerId, targetEntry.getTargetId());
        if (copyEffect.tokenWithHaste()) {
            // The copy *gains* haste — it is not printed on the copied card. Carrying it on the
            // entry (drained into the permanent's granted keywords at resolution) keeps it out of
            // the copied card's printed keywords, so the UI shows it as granted.
            copyEntry.getGrantedKeywordsOnEntry().add(Keyword.HASTE);
        }

        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", targetEntry.getCard(), " is created."));
        log.info("Game {} - {} copies {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());

        // Only the instant/sorcery-copy mode offers "you may choose new targets for the copy".
        if (!copyEffect.tokenCopy() && !copyEffect.tokenWithHaste() && copyEntry.getTargetId() != null) {
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
