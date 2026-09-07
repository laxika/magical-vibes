package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the sacrifice portion of Heart-Piercer Manticore's or Ziatora, the Incinerator's
 * trigger once the controller accepts the "may sacrifice".
 * Offers the controller's other creatures to sacrifice; the sacrifice creates the reflexive trigger
 * and its target is chosen by the choice-completion path
 * ({@code PermanentChoiceBattlefieldHandlerService.handleSacrificeAnotherCreatureDealPowerDamage}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrificeEffect = (SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Every creature the controller controls except this one ("another creature").
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard() != entry.getCard() && gameQueryService.isCreature(gameData, p)) {
                    validIds.add(p.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " has no other creature to sacrifice for ", entry.getCard(), "."));
            log.info("Game {} - {} has no other creature to sacrifice for {}",
                    gameData.id, playerName, entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeAnotherCreatureDealPowerDamage(
                        controllerId, entry.getCard(),
                        sacrificeEffect.reflexiveFollowUps()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose another creature to sacrifice.");

        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " is choosing a creature to sacrifice for ", entry.getCard(), "."));
        log.info("Game {} - {} choosing a creature to sacrifice for {}",
                gameData.id, playerName, entry.getCard().getName());
    }
}
