package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellForEachOtherControlledCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherControlledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopySpellForEachOtherControlledCreatureEffect) effect;
        if (e.spellSnapshot() == null) return;

        StackEntry spellSnapshot = e.spellSnapshot();
        UUID castingPlayerId = e.castingPlayerId();
        UUID originalTargetId = e.originalTargetId();
        Card spellCard = spellSnapshot.getCard();

        // CR 706.2 — a spell that "can't be copied" is not copied.
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((controllerId, perm) -> {
            if (!controllerId.equals(castingPlayerId)) return;
            if (perm.getId().equals(originalTargetId)) return;
            if (!gameQueryService.isCreature(gameData, perm)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spellCard, castingPlayerId)) return;

            eligibleTargets.add(perm);
        });

        for (Permanent target : eligibleTargets) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId());

            gameData.stack.add(copyEntry);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .text("A copy of ").card(spellCard)
                    .text(" is created targeting ").card(target.getCard()).text(".")
                    .build());
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other creature controlled by the caster",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(), spellCard.getName());
    }
}
