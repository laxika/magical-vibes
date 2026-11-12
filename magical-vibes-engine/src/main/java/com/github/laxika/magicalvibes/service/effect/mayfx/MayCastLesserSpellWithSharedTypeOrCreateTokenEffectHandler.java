package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastLesserSpellWithSharedTypeOrCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("mayCastLesserSpellWithSharedTypeOrCreateTokenMayEffectHandler")
@RequiredArgsConstructor
public class MayCastLesserSpellWithSharedTypeOrCreateTokenEffectHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService additionalSpellCostService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport exileCastTargetSupport;
    private final com.github.laxika.magicalvibes.service.effect.EffectResolutionService effectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastLesserSpellWithSharedTypeOrCreateTokenEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastLesserSpellWithSharedTypeOrCreateTokenEffect castEffect = ability.effects().stream()
                .filter(MayCastLesserSpellWithSharedTypeOrCreateTokenEffect.class::isInstance)
                .map(MayCastLesserSpellWithSharedTypeOrCreateTokenEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && (!additionalSpellCostService.satisfiableWithoutManaCost(
                gameData, player.getId(), ability.sourceCard())
                || (EffectResolution.needsSpellCastTarget(ability.sourceCard().getCastingCostsAndSpellEffects(),
                ability.sourceCard().isAuraThatRequiresAttachment(), ability.sourceCard().isEnchantPlayer())
                || EffectResolution.needsSpellTarget(ability.sourceCard().getCastingCostsAndSpellEffects()))
                && ability.sourceCard().getMinTargets() > 0
                && exileCastTargetSupport.firstSlotCandidates(gameData, ability.sourceCard(), player.getId()).isEmpty())) {
            accepted = false;
        }

        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .anyMatch(MayCastLesserSpellWithSharedTypeOrCreateTokenEffect.class::isInstance));
            mayCastHandlerService.handleMayCastFromHandWithoutPaying(gameData, player, true, ability, false);
            return;
        }

        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " declines to cast ", ability.sourceCard(), "."));
        if (gameData.pendingMayAbilities.stream().anyMatch(pending -> pending.effects().stream()
                .anyMatch(MayCastLesserSpellWithSharedTypeOrCreateTokenEffect.class::isInstance))) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card sourceCard = ability.sourceCard();
        Permanent source = ability.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (source != null) {
            sourceCard = source.getCard();
        }

        // "If you don't, create ..." happens as the ability that asked resolves (CR 608.2c), with what the question
        // kept of the spell cast.
        StackEntry tokenEntry = ReflexiveAbilitySupport.mayAbilityRecord(ability, sourceCard,
                List.of(castEffect.tokenEffect()), sourceCard.getName() + "'s ability", ability.sourcePermanentId(),
                0);
        effectResolutionService.resolveNestedInParkedResolution(gameData, tokenEntry);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
