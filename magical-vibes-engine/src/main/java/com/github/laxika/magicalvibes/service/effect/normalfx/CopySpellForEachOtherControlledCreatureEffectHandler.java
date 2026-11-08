package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CopySpellForEachOtherControlledCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final CopySupport copySupport;
    private final PlayerInputService playerInputService;

    @Autowired
    public CopySpellForEachOtherControlledCreatureEffectHandler(GameLogService gameLogService,
                                                                GameQueryService gameQueryService,
                                                                ValidTargetService validTargetService,
                                                                CopySupport copySupport,
                                                                PlayerInputService playerInputService) {
        this.gameLogService = gameLogService;
        this.gameQueryService = gameQueryService;
        this.validTargetService = validTargetService;
        this.copySupport = copySupport;
        this.playerInputService = playerInputService;
    }

    public CopySpellForEachOtherControlledCreatureEffectHandler(GameLogService gameLogService,
                                                                GameQueryService gameQueryService,
                                                                ValidTargetService validTargetService,
                                                                CopySupport copySupport) {
        this(gameLogService, gameQueryService, validTargetService, copySupport, null);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherControlledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopySpellForEachOtherControlledCreatureEffect) effect;
        StackEntry spellSnapshot = CopySupport.triggeringSpell(gameData, entry);
        // "Each other creature": other than this creature, the ability's source.
        UUID originalTargetId = entry.getSourcePermanentId();
        if (spellSnapshot == null || originalTargetId == null) return;

        UUID castingPlayerId = spellSnapshot.getControllerId();
        Card spellCard = spellSnapshot.getCard();

        // CR 707.10 — a spell that "can't be copied" is not copied.
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        List<Permanent> eligibleTargets = eligibleTargets(gameData, spellCard, castingPlayerId, originalTargetId);
        if (eligibleTargets.isEmpty()) {
            return;
        }

        if (e.chooseOne()) {
            if (eligibleTargets.size() == 1) {
                createCopy(gameData, spellSnapshot, castingPlayerId, eligibleTargets.getFirst());
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.CopySpellForOtherControlledCreatureChoice(e));
            playerInputService.beginPermanentChoice(gameData, castingPlayerId,
                    new ArrayList<>(eligibleTargets.stream().map(Permanent::getId).toList()),
                    entry.getCard().getName() + " — Choose another creature to copy the spell onto.");
            return;
        }

        for (Permanent target : eligibleTargets) {
            createCopy(gameData, spellSnapshot, castingPlayerId, target);
        }

        log.info("Game {} - {} triggers, creating {} copies of {} for each other creature controlled by the caster",
                gameData.id, entry.getCard().getName(), eligibleTargets.size(), spellCard.getName());
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.CopySpellForOtherControlledCreatureChoice context) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) return;
        StackEntry spell = CopySupport.triggeringSpell(gameData, pendingEntry);
        UUID originalTargetId = pendingEntry.getSourcePermanentId();
        if (spell == null || originalTargetId == null) return;

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null || eligibleTargets(gameData, spell.getCard(), spell.getControllerId(), originalTargetId)
                .stream().noneMatch(permanent -> permanent.getId().equals(chosenPermanentId))) {
            return;
        }

        createCopy(gameData, spell, spell.getControllerId(), chosen);
    }

    private List<Permanent> eligibleTargets(GameData gameData, Card spellCard,
                                            UUID castingPlayerId, UUID originalTargetId) {
        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((controllerId, perm) -> {
            if (!controllerId.equals(castingPlayerId)) return;
            if (perm.getId().equals(originalTargetId)) return;
            if (!gameQueryService.isCreature(gameData, perm)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(gameData, perm, spellCard, castingPlayerId)) return;

            eligibleTargets.add(perm);
        });
        return eligibleTargets;
    }

    private void createCopy(GameData gameData, StackEntry spellSnapshot, UUID castingPlayerId,
                            Permanent target) {
        Card spellCard = spellSnapshot.getCard();
        Card copyCard = copySupport.createCopyCard(spellCard);
        StackEntry copyEntry = spellSnapshot.getDamageAssignments().isEmpty()
                ? copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId())
                : copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, target.getId(),
                        Map.of(target.getId(), spellSnapshot.getDamageAssignments().values().stream()
                                .mapToInt(Integer::intValue).sum()),
                        List.of(target.getId()));
        copyEntry.setDeclaredTargetIds(spellSnapshot.getDeclaredTargetIds().stream()
                .map(ignored -> target.getId()).toList());

        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.builder()
                .text("A copy of ").card(spellCard)
                .text(" is created targeting ").card(target.getCard()).text(".")
                .build());
    }
}
