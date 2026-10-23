package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTakeDamageSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMayTakeDamageSacrificeSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Vexing Devil ETB — each opponent may accept to take the damage. Accepting deals damage from the
 * source; after every opponent has chosen, the source is sacrificed if anyone accepted (even when
 * the damage was prevented). A decline changes nothing before it prompts the next opponent or, once
 * an earlier opponent accepted, sacrifices the source, so a source with competing death replacements
 * is asked about before it leaves (CR 616.1) and the decline is completed again with that choice. An
 * accepting last opponent is dealt the damage first, so that sacrifice can't be redone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMayTakeDamageSacrificeSourceHandler implements MayEffectHandlerBean {

    private final AnyOpponentMayTakeDamageSacrificeSourceEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMayTakeDamageSacrificeSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            resolve(gameData, player, true, ability);
        } else {
            permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                    gameData, player, ability, () -> resolve(gameData, player, false, ability));
        }
    }

    private void resolve(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyOpponentMayTakeDamageSacrificeSourceEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        boolean anyAccepted = effect.anyAccepted();
        if (accepted) {
            effectHandler.dealDamage(gameData, ability, effect, chooserId);
            anyAccepted = true;
            log.info("Game {} - {} accepts {} damage from {}", gameData.id, player.getUsername(),
                    effect.damage(), ability.sourceCard().getName());
        } else {
            log.info("Game {} - {} declines {} damage from {}", gameData.id, player.getUsername(),
                    effect.damage(), ability.sourceCard().getName());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(chooserId);
        // Drop any ids that are no longer in the game (defensive).
        remaining.removeIf(id -> !gameData.playerIds.contains(id));

        if (!remaining.isEmpty()) {
            effectHandler.promptNext(gameData, ability.sourceCard(),
                    new AnyOpponentMayTakeDamageSacrificeSourceEffect(
                            effect.damage(),
                            List.copyOf(remaining),
                            effect.abilityControllerId(),
                            effect.sourcePermanentId(),
                            anyAccepted));
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (anyAccepted) {
            effectHandler.sacrificeSource(gameData, ability, effect);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
