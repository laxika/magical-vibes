package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Starts a cast of a copy that is currently in exile, optionally using a specified mana cost. */
@Slf4j
@Component
public class ExileNormalCostCopySupport {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final SpellCastingService spellCastingService;
    private final CastingCostService castingCostService;
    private final PotentialManaService potentialManaService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public ExileNormalCostCopySupport(GameLogService gameLogService,
                                      @Lazy PlayerInputService playerInputService,
                                      @Lazy InputCompletionService inputCompletionService,
                                      ExileCastTargetSupport exileCastTargetSupport,
                                      @Lazy SpellCastingService spellCastingService,
                                      @Lazy CastingCostService castingCostService,
                                      @Lazy PotentialManaService potentialManaService,
                                      @Lazy InteractionHandlerRegistry interactionHandlerRegistry) {
        this.castingCostService = castingCostService;
        this.potentialManaService = potentialManaService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.inputCompletionService = inputCompletionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.spellCastingService = spellCastingService;
    }

    public void offerCast(GameData gameData, Player player, Card copy) {
        offerCast(gameData, player, copy, null);
    }

    public void offerCast(GameData gameData, Player player, Card copy, String manaCostOverride) {
        offerCast(gameData, player, copy, manaCostOverride, null);
    }

    /**
     * Starts the cast of a copy. When the cost being paid contains {X} and {@code ability} is
     * supplied, the caster first announces X (CR 601.2b) through an X-value prompt that resumes in
     * {@link #resumeCastWithX}; otherwise X is 0.
     */
    public void offerCast(GameData gameData, Player player, Card copy, String manaCostOverride,
                          PendingMayAbility ability) {
        if (gameData.findExiledCard(copy.getId()) == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (manaCostOverride != null) {
            gameData.exilePlayCostModifiers.put(copy.getId(),
                    new ExilePlayCostModifier(player.getId(), null, 0, manaCostOverride));
        }

        if (ability != null && beginXValueChoice(gameData, player, copy, manaCostOverride, ability)) {
            return;
        }
        continueCast(gameData, player, copy, 0);
    }

    /**
     * Opens the "choose a value for X" prompt for casting {@code copy} while paying a mana cost
     * that contains {X}. Returns false (and opens nothing) when the cost has no {X}. The cap comes
     * from potential mana so an untapped board still opens the prompt.
     */
    public boolean beginXValueChoice(GameData gameData, Player player, Card copy, String manaCostOverride,
                                     PendingMayAbility ability) {
        String costStr = manaCostOverride != null ? manaCostOverride : copy.getManaCost();
        if (costStr == null || costStr.isBlank()) {
            return false;
        }
        ManaCost cost = new ManaCost(costStr);
        if (!cost.hasX()) {
            return false;
        }
        int tax = castingCostService.getCastCostModifier(gameData, player.getId(), copy, 0, Zone.EXILE);
        int maxX = cost.calculateMaxX(potentialManaService.buildVirtualManaPool(gameData, player.getId()), tax);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                player.getId(), 0, Math.max(0, maxX), "Choose a value for X to cast "
                + copy.getName() + ".", copy.getName(), true, costStr, ability));
        return true;
    }

    /** Resumes a normal-cost copy cast once the caster has announced X. */
    public void resumeCastWithX(GameData gameData, Player player, PendingMayAbility ability, int xValue) {
        Card copy = ability.sourceCard();
        if (gameData.findExiledCard(copy.getId()) == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        continueCast(gameData, player, copy, xValue);
    }

    private void continueCast(GameData gameData, Player player, Card copy, int xValue) {
        if (EffectResolution.needsTarget(copy)) {
            List<UUID> candidates = exileCastTargetSupport.firstSlotCandidates(
                    gameData, copy, player.getId());
            boolean legalTargets = copy.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, copy, player.getId())
                    : !candidates.isEmpty();
            if (!legalTargets) {
                gameData.removeFromExile(copy.getId());
                gameLogService.append(gameData, GameLog.cardThen(copy,
                        " has no valid targets and the copy ceases to exist."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            List<CardEffect> spellEffects = new ArrayList<>(copy.getEffects(EffectSlot.SPELL));
            StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(copy);
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    copy, player.getId(), spellEffects, spellType, true, List.of(), 0, false, 0, false, true,
                    null, null, xValue));
            playerInputService.beginPermanentChoice(gameData, player.getId(), candidates,
                    "Choose a target for " + copy.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(
                    gameData, player, copy.getId(), xValue, (UUID) null, true);
        } catch (IllegalStateException ex) {
            gameData.removeFromExile(copy.getId());
            log.info("Game {} - cast of copy {} could not be completed",
                    gameData.id, copy.getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
