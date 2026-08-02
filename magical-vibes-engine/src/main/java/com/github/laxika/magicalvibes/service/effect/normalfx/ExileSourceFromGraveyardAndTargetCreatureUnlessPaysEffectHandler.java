package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect} by handing the
 * decision to the target creature's controller: they may pay the mana to stop the exile, and
 * declining (or being unable to pay) exiles both the creature and the source card. Paying mana is
 * always a choice, so the prompt is offered even when the pool looks empty — an accept without the
 * mana falls through to the exile in the may-choice handler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileSourceFromGraveyardAndTargetCreatureUnlessPaysEffect) effect;

        UUID targetPermanentId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            // Target left the battlefield before resolution — the ability does nothing and the
            // source card stays in the graveyard.
            log.info("Game {} - {}'s ability fizzles (target gone)", gameData.id, entry.getCard().getName());
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        if (targetControllerId == null) {
            return;
        }

        String prompt = "Pay " + e.manaCost() + "? If you don't, " + target.getCard().getName()
                + " and " + entry.getCard().getName() + " are exiled.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(e), prompt,
                targetPermanentId, e.manaCost(), entry.getSourcePermanentId()));
    }
}
