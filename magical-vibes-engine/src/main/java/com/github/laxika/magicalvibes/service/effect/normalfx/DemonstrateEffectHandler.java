package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles the resolution-time opponent choice made by demonstrate. */
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
        UUID spellId = entry.getTriggeringCardId();
        if (spellId == null) {
            return;
        }

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> spellId.equals(candidate.getTargetableId()))
                .findFirst()
                .map(StackEntry::new)
                .orElse(null);
        if (spell == null) {
            return;
        }
        if (spell.getCard().isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spell.getCard().getName());
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(spell.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DemonstrateOpponentChoice(spell));
        playerInputService.beginPlayerChoice(
                gameData,
                spell.getControllerId(),
                opponents,
                spell.getCard().getName() + " — Choose an opponent to copy it.");
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
                                       PermanentChoiceContext.DemonstrateOpponentChoice context) {
        StackEntry spell = context.spellSnapshot();
        UUID controllerId = spell.getControllerId();
        if (!gameData.playerIds.contains(opponentId) || opponentId.equals(controllerId)) {
            throw new IllegalStateException("Demonstrate requires an opponent");
        }
        if (spell.getCard().isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spell.getCard().getName());
            return;
        }

        createCopy(gameData, spell, controllerId);
        createCopy(gameData, spell, opponentId);
    }

    private void createCopy(GameData gameData, StackEntry spell, UUID controllerId) {
        Card spellCard = spell.getCard();
        Card copyCard = copySupport.createCopyCard(spellCard);
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                spell, copyCard, controllerId, spell.getTargetId());
        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
        log.info("Game {} - copy of {} created for {}", gameData.id, spellCard.getName(), controllerId);

        if (copyEntry.getTargetId() != null) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    spellCard,
                    controllerId,
                    List.of(new CopySpellEffect()),
                    "Choose new targets for the copy of " + spellCard.getName() + "?",
                    copyCard.getId()));
        }
    }
}
