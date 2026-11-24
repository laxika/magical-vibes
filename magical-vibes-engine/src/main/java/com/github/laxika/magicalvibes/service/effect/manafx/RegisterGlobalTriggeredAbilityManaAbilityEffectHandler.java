package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Registers {@link RegisterGlobalTriggeredAbilityEffect}'s ability for a mana ability ("{T}: Add {C}{C}. When you next
 * cast a spell with {X} in its mana cost ... this turn, copy that spell or ability"): controlled by the player who
 * activated the mana ability, with the permanent as its source (CR 603.7d, 603.7e). A mana ability resolves on no
 * stack, so each activation creates its own.
 */
@Slf4j
@Component
public class RegisterGlobalTriggeredAbilityManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        var registration = (RegisterGlobalTriggeredAbilityEffect) effect;
        boolean untilNextTurn = Duration.UNTIL_YOUR_NEXT_TURN.equals(registration.duration());
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(playerId,
                permanent.getCard(), (TriggeredAbility) registration.grantedAbility(), null, false, untilNextTurn,
                untilNextTurn ? gameData.turnNumber : -1, null, null, permanent.getId(), null, null, false,
                registration.next()));
        log.info("Game {} - {} registers a delayed triggered ability with a mana ability", gameData.id, playerId);
    }
}
