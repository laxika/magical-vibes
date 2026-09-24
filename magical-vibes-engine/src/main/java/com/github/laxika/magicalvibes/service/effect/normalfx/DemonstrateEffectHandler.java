package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemonstrateEffectHandler implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DemonstrateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .findFirst()
                .orElse(null);
        if (spell == null || spell.isCopy()) {
            return;
        }

        Card spellCard = spell.getCard();
        if (spellCard.isCantBeCopied()) {
            return;
        }

        StackEntry spellSnapshot = new StackEntry(spell);
        createCopy(gameData, spellSnapshot, spellSnapshot.getControllerId());

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }
        if (opponents.size() == 1) {
            createCopy(gameData, spellSnapshot, opponents.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.DemonstrateOpponentChoice(
                spellSnapshot, entry.getControllerId()));
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), opponents,
                spellCard.getName() + " — choose an opponent to copy it.");
    }

    public void completeOpponentChoice(GameData gameData, UUID chosenOpponentId,
                                       PermanentChoiceContext.DemonstrateOpponentChoice context) {
        if (!gameData.playerIds.contains(chosenOpponentId)
                || chosenOpponentId.equals(context.controllerId())) {
            return;
        }
        createCopy(gameData, context.spellSnapshot(), chosenOpponentId);
    }

    private void createCopy(GameData gameData, StackEntry spellSnapshot, UUID controllerId) {
        Card copyCard = copySupport.createCopyCard(spellSnapshot.getCard());
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                spellSnapshot, copyCard, controllerId, spellSnapshot.getTargetId());
        copySupport.addCopyToStack(gameData, copyEntry);
        gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellSnapshot.getCard(), " is created."));
        log.info("Game {} - demonstrate creates a copy of {} for {}",
                gameData.id, spellSnapshot.getCard().getName(), controllerId);
    }
}
