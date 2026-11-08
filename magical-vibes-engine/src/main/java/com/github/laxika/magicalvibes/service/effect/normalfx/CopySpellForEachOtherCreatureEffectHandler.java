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
        StackEntry spell = CopySupport.triggeringSpell(gameData, entry);
        UUID sourceId = entry.getSourcePermanentId();
        if (spell == null || sourceId == null) return;
        // The intervening "if" is checked again as the ability resolves (CR 603.4): the spell must still target only
        // this creature.
        if (!sourceId.equals(CopySupport.soleTargetId(spell))) return;

        Card spellCard = spell.getCard();
        UUID castingPlayerId = spell.getControllerId();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.getId().equals(sourceId)) return;
            if (!gameQueryService.isCreature(gameData, permanent)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(
                    gameData, permanent, spellCard, castingPlayerId, spell.getXValue())) return;
            eligibleTargets.add(permanent);
        });

        for (Permanent target : eligibleTargets) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    spell, copyCard, entry.getControllerId(), target.getId());
            for (int i = 0; i < copyEntry.getDeclaredTargetIds().size(); i++) {
                if (sourceId.equals(copyEntry.getDeclaredTargetIds().get(i))) {
                    copyEntry.replaceTargetIdAt(i, target.getId());
                }
            }
            if (!copyEntry.getDamageAssignments().isEmpty()) {
                int damage = copyEntry.getDamageAssignments().values().stream().mapToInt(Integer::intValue).sum();
                copyEntry.getDamageAssignments().clear();
                copyEntry.getDamageAssignments().put(target.getId(), damage);
            }
            copySupport.addCopyToStack(gameData, copyEntry);
            gameLogService.append(gameData, GameLog.builder()
                    .text("A copy of ").card(spellCard)
                    .text(" is created targeting ").card(target.getCard()).text(".").build());
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other creature",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(), spellCard.getName());
    }
}
