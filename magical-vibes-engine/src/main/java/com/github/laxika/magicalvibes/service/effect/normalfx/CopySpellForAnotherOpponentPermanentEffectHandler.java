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
        if (copyEffect.spellSnapshot() == null) return;

        StackEntry spellSnapshot = copyEffect.spellSnapshot();
        Card spellCard = spellSnapshot.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        List<Permanent> eligibleTargets = eligibleTargets(gameData, copyEffect, entry.getControllerId());
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
        CopySpellForAnotherOpponentPermanentEffect effect = context.effect();
        if (pendingEntry == null || effect.spellSnapshot() == null) return;

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null || eligibleTargets(gameData, effect, pendingEntry.getControllerId()).stream()
                .noneMatch(permanent -> permanent.getId().equals(chosenPermanentId))) {
            return;
        }

        createCopy(gameData, effect.spellSnapshot(), pendingEntry.getControllerId(), chosen);
    }

    private List<Permanent> eligibleTargets(GameData gameData,
                                             CopySpellForAnotherOpponentPermanentEffect effect,
                                             UUID sourceControllerId) {
        List<Permanent> eligibleTargets = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (controllerId.equals(sourceControllerId)
                    || controllerId.equals(effect.originalTargetControllerId())) return;
            if (!gameData.playerIds.contains(controllerId) || gameQueryService.isLand(gameData, permanent)) return;
            if (!validTargetService.canPermanentBeTargetedBySpell(
                    gameData, permanent, effect.spellSnapshot().getCard(), effect.castingPlayerId())) return;
            eligibleTargets.add(permanent);
        });
        return eligibleTargets;
    }

    private void createCopy(GameData gameData, StackEntry spellSnapshot, UUID controllerId, Permanent target) {
        Card copyCard = copySupport.createCopyCard(spellSnapshot.getCard());
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                spellSnapshot, copyCard, controllerId, target.getId());
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
