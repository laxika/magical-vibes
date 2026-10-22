package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyDrawnInstantOrSorceryAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles casting or declining God-Eternal Kefnet's reduced-cost copy. */
@Component
@RequiredArgsConstructor
public class CopyDrawnInstantOrSorceryAndMayCastCopyHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SpellCastingService spellCastingService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final InputCompletionService inputCompletionService;
    private final CastingCostService castingCostService;
    private final PotentialManaService potentialManaService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyDrawnInstantOrSorceryAndMayCastCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        handle(gameData, player, accepted, ability, null);
    }

    /** Resumes the cast after the controller chose the value of X for the copy. */
    public void resumeCastWithX(GameData gameData, Player player, PendingMayAbility ability, int chosenX) {
        handle(gameData, player, true, ability, chosenX);
    }

    private void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability,
                        Integer chosenX) {
        Card copy = ability.sourceCard();
        if (!accepted) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " declines to cast the copy of ", copy, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (EffectResolution.needsTarget(copy)) {
            boolean hasLegalTargets = copy.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, copy, player.getId())
                    : !exileCastTargetSupport.firstSlotCandidates(gameData, copy, player.getId()).isEmpty();
            if (!hasLegalTargets) {
                gameData.removeFromExile(copy.getId());
                gameLogService.append(gameData,
                        GameLog.textCardText("The copy of ", copy, " has no legal targets and ceases to exist."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        if (chosenX == null && copy.getParsedManaCost() != null && copy.getParsedManaCost().hasX()) {
            int tax = castingCostService.getCastCostModifier(gameData, player.getId(), copy, 0, Zone.EXILE);
            int maxX = copy.getParsedManaCost().calculateMaxX(
                    potentialManaService.buildVirtualManaPool(gameData, player.getId()), tax);
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    player.getId(), 0, Math.max(0, maxX),
                    "Choose a value for X to cast the copy of " + copy.getName() + ".",
                    copy.getName(), true, copy.getManaCost(), ability));
            return;
        }
        int xValue = chosenX == null ? 0 : chosenX;
        try {
            spellCastingService.paySpellManaCostFromNonHandZone(gameData, player.getId(), copy, xValue, Zone.EXILE);
        } catch (IllegalStateException ex) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " cannot pay the reduced cost for the copy of ", copy, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, player.getId(), List.of(copy.getId()), xValue);
    }
}
