package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureBecomesChosenSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureBecomesChosenSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId(), Set.of());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        Permanent aura = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent enchanted = aura == null || aura.getAttachedTo() == null
                ? null : gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            return;
        }

        enchanted.setTransientCreatureTypeOverride(chosenSubtype);
        enchanted.getTransientCreatureTypeOverrides().clear();
        gameLogService.append(gameData, GameLog.builder()
                .card(enchanted.getCard())
                .text(" becomes a " + chosenSubtype.getDisplayName() + " until end of turn.")
                .build());
    }
}
