package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedChooseOpponentGainsControlOfSource;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Rainbow Vale's "an opponent gains control of this land at the beginning of the next end step" as part of its
 * mana ability: the delayed trigger is created as the ability resolves, controlled by the player who activated it (its
 * ruling: that player chooses the opponent, whoever controls the land then).
 */
@Slf4j
@Component
public class RegisterDelayedChooseOpponentGainsControlOfSourceManaAbilityEffectHandler
        implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedChooseOpponentGainsControlOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        if (permanent == null) {
            return;
        }
        gameData.queueDelayedAction(new DelayedChooseOpponentGainsControlOfSource(
                playerId, permanent.getId(), permanent.getCard()));
        log.info("Game {} - {} registers its control change at the next end step", gameData.id,
                permanent.getCard().getName());
    }
}
