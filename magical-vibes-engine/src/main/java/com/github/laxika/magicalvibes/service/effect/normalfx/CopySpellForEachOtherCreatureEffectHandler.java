package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellForEachOtherCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopySpellForEachOtherCreatureEffect) effect;
        if (e.spellSnapshot() == null) return;

        StackEntry spellSnapshot = e.spellSnapshot();
        Card spellCard = spellSnapshot.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.getId().equals(e.originalTargetId())) return;
            if (!gameQueryService.isCreature(gameData, permanent)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(
                    gameData, permanent, spellCard, e.castingPlayerId())) return;
            eligibleTargets.add(permanent);
        });

        for (Permanent target : eligibleTargets) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    spellSnapshot, copyCard, entry.getControllerId(), target.getId());
            copySupport.addCopyToStack(gameData, copyEntry);
            gameLogService.append(gameData, GameLog.builder()
                    .text("A copy of ").card(spellCard)
                    .text(" is created targeting ").card(target.getCard()).text(".").build());
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other creature",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(), spellCard.getName());
    }
}
