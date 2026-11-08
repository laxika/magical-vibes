package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForAnotherOpponentPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class CopySpellForAnotherOpponentPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final CopySupport copySupport;
    private final PlayerInputService playerInputService;

    @Autowired
    public CopySpellForAnotherOpponentPermanentEffectHandler(GameLogService gameLogService,
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForAnotherOpponentPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CopySpellForAnotherOpponentPermanentEffect) effect;
        StackEntry spellSnapshot = CopySupport.triggeringSpell(gameData, entry);
        if (spellSnapshot == null) return;

        Card spellCard = spellSnapshot.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        List<Permanent> eligibleTargets = eligibleTargets(gameData, entry, spellSnapshot);
        if (eligibleTargets.isEmpty()) return;

        if (eligibleTargets.size() == 1) {
            createCopy(gameData, spellSnapshot, entry.getControllerId(), eligibleTargets.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.CopySpellForAnotherOpponentPermanentChoice(copyEffect));
        playerInputService.beginPermanentChoice(
                gameData,
                entry.getControllerId(),
                new ArrayList<>(eligibleTargets.stream().map(Permanent::getId).toList()),
                entry.getCard().getName() + " — Choose a permanent to copy the spell onto.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.CopySpellForAnotherOpponentPermanentChoice context) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) return;
        StackEntry spellSnapshot = CopySupport.triggeringSpell(gameData, pendingEntry);
        if (spellSnapshot == null) return;

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null || eligibleTargets(gameData, pendingEntry, spellSnapshot).stream()
                .noneMatch(permanent -> permanent.getId().equals(chosenPermanentId))) {
            return;
        }

        createCopy(gameData, spellSnapshot, pendingEntry.getControllerId(), chosen);
    }

    /**
     * The nonland permanents the spell could target that are controlled by an opponent of the ability's controller
     * other than the one controlling the permanent the spell targeted as the ability triggered (as that permanent last
     * existed, if it has left the battlefield).
     */
    private List<Permanent> eligibleTargets(GameData gameData, StackEntry entry, StackEntry spellSnapshot) {
        UUID sourceControllerId = entry.getControllerId();
        UUID originalTargetControllerId = originalTargetController(gameData,
                CopySupport.soleTargetId(entry.getTriggeringSpellSnapshot()));
        Set<UUID> spellTargetableIds = Set.copyOf(validTargetService.computeValidTargetsForSpell(
                gameData, spellSnapshot.getCard(), spellSnapshot.getControllerId(), List.of(),
                spellSnapshot.getXValue(), null).validPermanentIds());
        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (controllerId.equals(sourceControllerId)
                    || controllerId.equals(originalTargetControllerId)) return;
            if (!gameData.playerIds.contains(controllerId) || gameQueryService.isLand(gameData, permanent)) return;
            if (!spellTargetableIds.contains(permanent.getId())) return;
            eligibleTargets.add(permanent);
        });
        return eligibleTargets;
    }

    private UUID originalTargetController(GameData gameData, UUID originalTargetId) {
        if (originalTargetId == null) {
            return null;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, originalTargetId);
        if (controllerId != null) {
            return controllerId;
        }
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(departed -> departed.getId().equals(originalTargetId))
                .findFirst()
                .map(departed -> gameData.departedPermanentControllerIds.get(departed.getCard().getId()))
                .orElse(null);
    }

    private void createCopy(GameData gameData, StackEntry spellSnapshot, UUID controllerId, Permanent target) {
        Card copyCard = copySupport.createCopyCard(spellSnapshot.getCard());
        Map<UUID, Integer> damageAssignments = spellSnapshot.getDamageAssignments().isEmpty()
                ? Map.of()
                : Map.of(target.getId(), spellSnapshot.getDamageAssignments().values().stream()
                        .mapToInt(Integer::intValue).sum());
        StackEntry copyEntry = damageAssignments.isEmpty()
                ? copySupport.createCopyStackEntry(spellSnapshot, copyCard, controllerId, target.getId())
                : copySupport.createCopyStackEntry(spellSnapshot, copyCard, controllerId, target.getId(),
                        damageAssignments, List.of(target.getId()));
        if (!spellSnapshot.getDeclaredTargetIds().isEmpty()) {
            copyEntry.setDeclaredTargetIds(spellSnapshot.getDeclaredTargetIds().stream()
                    .map(ignored -> target.getId())
                    .toList());
        }
        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.builder()
                .text("A copy of ").card(spellSnapshot.getCard())
                .text(" is created targeting ").card(target.getCard()).text(".")
                .build());
    }
}
