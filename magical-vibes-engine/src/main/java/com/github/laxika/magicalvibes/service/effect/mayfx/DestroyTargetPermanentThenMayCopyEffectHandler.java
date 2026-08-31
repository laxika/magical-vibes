package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenMayCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component("destroyTargetPermanentThenMayCopyMayEffectHandler")
@RequiredArgsConstructor
public class DestroyTargetPermanentThenMayCopyEffectHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentThenMayCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card spellCard = pendingEntry.getCard();
        if (!spellCard.isCantBeCopied()) {
            StackEntry spellSnapshot = new StackEntry(pendingEntry);
            Card copyCard = copySupport.createCopyCard(spellCard);
            UUID controllerId = ability.controllerId();
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    spellSnapshot, copyCard, controllerId, spellSnapshot.getTargetId());
            copySupport.addCopyToStack(gameData, copyEntry);

            gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
            log.info("Game {} - copy of {} created for target permanent controller",
                    gameData.id, spellCard.getName());

            if (copyEntry.getTargetId() != null) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        spellCard,
                        controllerId,
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + spellCard.getName() + "?",
                        copyCard.getId()
                ));
            }
        } else {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
