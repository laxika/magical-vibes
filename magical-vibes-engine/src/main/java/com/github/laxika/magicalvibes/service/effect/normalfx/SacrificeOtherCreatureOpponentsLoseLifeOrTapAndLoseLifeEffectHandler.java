package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
                String cardName = entry.getCard().getName();
                UUID sourcePermanentId = entry.getSourcePermanentId();

                // "Other than this creature" compares object identity: a Demon that left and returned is a
                // new object (CR 400.7) and may be sacrificed to the trigger of its previous incarnation.
                List<UUID> otherCreatureIds = destructionSupport.collectCreatureIds(gameData, controllerId,
                        p -> !p.getId().equals(sourcePermanentId)
                                && !gameQueryService.cantBeChosenToSacrifice(gameData, p));

                if (otherCreatureIds.isEmpty()) {
                    // Can't sacrifice — tap source and controller loses life
                    Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                    if (sourcePermanent != null) {
                        sourcePermanent.tap();
                        String tapLog = cardName + " is tapped.";
                        gameLogService.append(gameData, GameLog.text(tapLog));
                        log.info("Game {} - {} is tapped (no creature to sacrifice)", gameData.id, cardName);
                    }
                    // The life loss is a step of its own, so a death replacement question its Lich's Duel Mastery
                    // sacrifice asks (CR 616.1f) reruns only the loss, not the tap.
                    entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                            List.of(new LoseLifeEffect(e.lifeLoss(), LoseLifeRecipient.CONTROLLER)));
                    return;
                }

                if (otherCreatureIds.size() == 1) {
                    // Only one other creature — sacrifice it automatically
                    Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
                    if (creature != null) {
                        int power = gameQueryService.getEffectivePower(gameData, creature);
                        destructionSupport.sacrificeAndLog(gameData, creature, controllerId);
                        destructionSupport.applyOpponentsLoseLife(gameData, controllerId, power, cardName);
                    }
                    return;
                }

                // Multiple other creatures — prompt player to choose
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife(controllerId, cardName));
                playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                        "Choose a creature other than " + cardName + " to sacrifice.");
    }
}
