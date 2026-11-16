package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Creates {@link CopyNextInstantOrSorceryCastThisTurnEffect#delayedAbility()} for a mana ability: a delayed triggered ability that triggers only the next time
 * its event occurs this turn (CR 603.7b), controlled by the player who activated the mana ability and with its source
 * (CR 603.7e). A mana ability resolves on no stack, so each activation creates its own.
 */
@Slf4j
@Component
public class CopyNextInstantOrSorceryCastThisTurnManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantOrSorceryCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        var delayedEffect = (CopyNextInstantOrSorceryCastThisTurnEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(playerId,
                permanent.getCard(), delayedEffect.delayedAbility(), permanent.getId()));
        log.info("Game {} - {} will copy their next instant or sorcery spell this turn", gameData.id, playerId);
    }
}
