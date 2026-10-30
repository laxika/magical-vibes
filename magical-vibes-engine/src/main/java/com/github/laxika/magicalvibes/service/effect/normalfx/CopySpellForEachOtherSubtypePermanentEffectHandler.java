package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellForEachOtherSubtypePermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ValidTargetService validTargetService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherSubtypePermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopySpellForEachOtherSubtypePermanentEffect) effect;
        if (e.spellSnapshot() == null) return;

        StackEntry spellSnapshot = e.spellSnapshot();
        UUID castingPlayerId = e.castingPlayerId();
        UUID originalTargetId = e.originalTargetId();
        CardSubtype subtype = e.subtype();
        Card spellCard = spellSnapshot.getCard();

        // CR 707.10 — a spell that "can't be copied" is not copied.
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((pid, perm) -> {
            if (perm.getId().equals(originalTargetId)) return;
            if (!perm.getCard().getSubtypes().contains(subtype)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spellCard, castingPlayerId,
                    spellSnapshot.getXValue())) return;

            eligibleTargets.add(perm);
        });

        for (Permanent target : eligibleTargets) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = createCopyRetargetingEveryTarget(spellSnapshot, copyCard, castingPlayerId, target);

            copySupport.addCopyToStack(gameData, copyEntry);

            
            gameLogService.append(gameData, GameLog.builder().text("A copy of ").card(spellCard).text(" is created targeting ").card(target.getCard()).text(".").build());
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other {}",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(),
                spellCard.getName(), subtype.getDisplayName());
    }

    /**
     * The original targets all name the one Golem the spell targets, so every repeated target id and every
     * divided damage portion moves onto the new target.
     */
    private StackEntry createCopyRetargetingEveryTarget(StackEntry spellSnapshot, Card copyCard,
                                                        UUID castingPlayerId, Permanent target) {
        if (!spellSnapshot.getDamageAssignments().isEmpty()) {
            int totalDamage = spellSnapshot.getDamageAssignments().values().stream()
                    .mapToInt(Integer::intValue).sum();
            return copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId(),
                    Map.of(target.getId(), totalDamage), List.of(target.getId()));
        }
        if (spellSnapshot.getTargetIds().size() > 1) {
            return copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId(),
                    Map.of(), new ArrayList<>(Collections.nCopies(spellSnapshot.getTargetIds().size(), target.getId())));
        }
        return copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId());
    }
}
