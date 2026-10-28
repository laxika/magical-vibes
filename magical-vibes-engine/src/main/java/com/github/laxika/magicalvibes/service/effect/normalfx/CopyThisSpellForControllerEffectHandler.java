package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyThisSpellForControllerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyThisSpellForControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card spellCard = entry.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        Card copyCard = copySupport.createCopyCard(spellCard);
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                entry, copyCard, entry.getControllerId(), entry.getTargetId());
        // A copy of a spell isn't cast (CR 707.10), and the choice to copy is made on resolution, so
        // the copy carries no copy step of its own.
        copyEntry.getEffectsToResolve().removeIf(CopyThisSpellForControllerEffectHandler::isCopyStep);
        copySupport.addCopyToStack(gameData, copyEntry, copyEntry.getTargetId() == null ? null
                : new PendingMayAbility(
                        spellCard,
                        entry.getControllerId(),
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + spellCard.getName() + "?",
                        copyCard.getId()));

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
        log.info("Game {} - copy of {} created for its controller", gameData.id, spellCard.getName());
    }

    private static boolean isCopyStep(CardEffect effect) {
        CardEffect unwrapped = effect;
        while (true) {
            if (unwrapped instanceof MayEffect may) {
                unwrapped = may.wrapped();
            } else if (unwrapped instanceof ConditionalEffect conditional) {
                unwrapped = conditional.wrapped();
            } else {
                return unwrapped instanceof CopyThisSpellForControllerEffect;
            }
        }
    }
}
