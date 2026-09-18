package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandThenMayPayManaAndCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureToHandThenMayPayManaAndCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureToHandThenMayPayManaAndCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        StackEntry spellSnapshot = new StackEntry(entry);
        var bounceEffect = (ReturnTargetCreatureToHandThenMayPayManaAndCopyEffect) effect;
        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        MayEffect copyChoice = new MayEffect(
                new CopyControllerCastSpellEffect(spellSnapshot, targetControllerId),
                "Copy this spell?",
                null,
                MayChoicePlayer.CONTROLLER
        );
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetControllerId,
                List.of(copyChoice),
                entry.getCard().getName() + " - Pay " + bounceEffect.manaCost() + " to copy this spell?",
                null,
                bounceEffect.manaCost(),
                null
        ));
    }
}
