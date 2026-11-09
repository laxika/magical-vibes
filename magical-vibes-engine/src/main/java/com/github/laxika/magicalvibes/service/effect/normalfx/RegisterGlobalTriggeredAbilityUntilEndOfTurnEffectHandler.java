package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Stores a global trigger registered by a resolving spell until turn cleanup. The trigger remains
 * active if the registering spell has already moved to its owner's graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect) effect;
        CardEffect triggeredEffect = registration.triggeredEffect();
        if (triggeredEffect instanceof CopyImprintedCardAndMayCastCopyEffect copyEffect) {
            Card imprinted = gameData.getImprintedCard(entry.getCard());
            if (imprinted != null) {
                triggeredEffect = copyEffect.boundTo(imprinted.getId());
            }
        }
        TriggeredAbility ability = ((TriggeredAbility) registration.grantedAbility())
                .withEffects(List.of(new EffectRegistration(triggeredEffect)));
        // The registration records the registering source permanent and the player the resolving ability targets,
        // whatever the ability's effects are; the ability's filters read them (CR 603.7e).
        UUID watchedPlayerId = entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                ? entry.getTargetId() : null;
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), ability, null, false, false, -1, null, null,
                entry.getSourcePermanentId(), watchedPlayerId));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability until end of turn."));
        log.info("Game {} - {} registers a global triggered ability until end of turn",
                gameData.id, entry.getCard().getName());
    }
}
