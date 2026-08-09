package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Grim Reminder's name-matching opponent life-loss clause. */
@Component
@RequiredArgsConstructor
public class LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect loss =
                (LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect) effect;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }
            boolean castMatchingSpell = gameData.getSpellsCastThisTurn(playerId).stream()
                    .anyMatch(card -> loss.cardName().equals(card.getName()));
            if (castMatchingSpell) {
                lifeSupport.applyLifeLoss(gameData, playerId, loss.amount(), entry.getCard().getName());
            }
        }
    }
}
