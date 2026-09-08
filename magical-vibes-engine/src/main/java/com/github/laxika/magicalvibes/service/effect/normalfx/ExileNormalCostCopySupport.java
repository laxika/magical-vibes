package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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

    public ExileNormalCostCopySupport(GameLogService gameLogService,
                                      @Lazy PlayerInputService playerInputService,
                                      @Lazy InputCompletionService inputCompletionService,
                                      ExileCastTargetSupport exileCastTargetSupport,
                                      @Lazy SpellCastingService spellCastingService) {
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
        if (gameData.findExiledCard(copy.getId()) == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (manaCostOverride != null) {
            gameData.exilePlayCostModifiers.put(copy.getId(),
                    new ExilePlayCostModifier(player.getId(), null, 0, manaCostOverride));
        }

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
                    copy, player.getId(), spellEffects, spellType, true, List.of(), 0, true));
            playerInputService.beginPermanentChoice(gameData, player.getId(), candidates,
                    "Choose a target for " + copy.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(
                    gameData, player, copy.getId(), 0, (UUID) null, true);
        } catch (IllegalStateException ex) {
            gameData.removeFromExile(copy.getId());
            log.info("Game {} - cast of copy {} could not be completed",
                    gameData.id, copy.getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
