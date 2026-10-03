package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastUpToNSpellsFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.CastUpToNSpellsFromHandWithoutPayingManaCostEffectHandler;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles accepting or declining one card in a bounded free-cast choice group. */
@Component
@RequiredArgsConstructor
public class CastUpToNSpellsFromHandWithoutPayingManaCostHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;
    private final CastUpToNSpellsFromHandWithoutPayingManaCostEffectHandler offerHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastUpToNSpellsFromHandWithoutPayingManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastUpToNSpellsFromHandWithoutPayingManaCostEffect castEffect = ability.effects().stream()
                .filter(CastUpToNSpellsFromHandWithoutPayingManaCostEffect.class::isInstance)
                .map(CastUpToNSpellsFromHandWithoutPayingManaCostEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID choiceGroupId = castEffect.choiceGroupId();
        if (accepted && choiceGroupId != null) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .filter(CastUpToNSpellsFromHandWithoutPayingManaCostEffect.class::isInstance)
                    .map(CastUpToNSpellsFromHandWithoutPayingManaCostEffect.class::cast)
                    .anyMatch(pendingEffect -> choiceGroupId.equals(pendingEffect.choiceGroupId())));

            offerHandler.queueOffers(gameData, ability.controllerId(), ability.sourcePermanentId(),
                    castEffect.maxCount() - 1, choiceGroupId, ability.sourceCard().getId());
        }

        // Keep the other cards in this group when the shared hand-cast path completes.
        mayCastHandlerService.handleMayCastFromHandWithoutPaying(
                gameData, player, accepted, ability, false);
    }
}
