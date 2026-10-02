package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithNormalCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileReducedCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an accepted normal-cost cast offer from source-linked exile. */
@Component
@RequiredArgsConstructor
public class MayCastExiledCardWithNormalCostHandler implements MayEffectHandlerBean {

    private final ExileReducedCastSupport exileReducedCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final com.github.laxika.magicalvibes.service.spell.SpellCastingService spellCastingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastExiledCardWithNormalCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastExiledCardWithNormalCostEffect effect = ability.effects().stream()
                .filter(MayCastExiledCardWithNormalCostEffect.class::isInstance)
                .map(MayCastExiledCardWithNormalCostEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && ability.targetCardId() != null) {
            var exiled = gameData.findExiledCard(ability.targetCardId());
            if (exiled != null && exiled.card().hasType(com.github.laxika.magicalvibes.model.CardType.LAND)) {
                spellCastingService.playLandFromExileDuringResolution(gameData, player, ability.targetCardId());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            gameData.pendingMayAbilities.removeIf(pending -> pending != ability
                    && pending.effects().stream()
                    .anyMatch(candidate -> candidate instanceof MayCastExiledCardWithNormalCostEffect other
                            && other.offerGroupId().equals(effect.offerGroupId())));
            if (effect.anyManaType()) {
                gameData.exilePlayAnyManaType.add(ability.targetCardId());
            }
            Permanent source = ability.sourcePermanentId() == null
                    ? ability.sourcePermanentSnapshot()
                    : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            int genericCostReduction = effect.genericCostReduction() == null
                    ? 0
                    : Math.max(0, amountEvaluationService.evaluate(gameData,
                            effect.genericCostReduction(),
                            new AmountContext(player.getId(), source, null, 0, 0)));
            exileReducedCastSupport.castFromExileWithCostReduction(
                    gameData, player, ability.targetCardId(), genericCostReduction,
                    effect.putOnBottomOfOwnersLibraryInsteadOfGraveyard());
            return;
        }

        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " declines to cast ", ability.sourceCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
