package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfChosenTypeMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Resolves {@link CreaturesOfChosenTypeMustAttackThisTurnEffect} by prompting for a creature type
 * and registering a turn-long must-attack requirement for creatures of that type.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreaturesOfChosenTypeMustAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreaturesOfChosenTypeMustAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        // Not a characteristic or control change, so it modifies the game rules and also binds creatures
        // of that type that enter or gain the type later this turn (CR 611.2c).
        gameData.playerStaticEffectsUntilEndOfTurn
                .computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                .add(new MatchingCreaturesMustAttackEffect(new PermanentHasSubtypePredicate(chosenSubtype)));

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " forces " + chosenSubtype.getDisplayName() + " creatures to attack this turn if able."));
        log.info("Game {} - {} forces {} creatures to attack this turn if able",
                gameData.id, entry.getCard().getName(), chosenSubtype);
    }
}
