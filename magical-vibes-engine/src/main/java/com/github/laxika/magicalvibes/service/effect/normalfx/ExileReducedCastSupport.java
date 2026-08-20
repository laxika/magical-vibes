package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

/** Supports a may-cast-from-exile offer that pays the reduced mana cost. */
@Slf4j
@Component
public class ExileReducedCastSupport {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final SpellCastingService spellCastingService;

    public ExileReducedCastSupport(GameLogService gameLogService,
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

    public void castFromExileWithCostReduction(GameData gameData, Player player, UUID exileCardId,
                                               int genericCostReduction) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card card = exiledEntry.card();
        UUID playerId = player.getId();
        installCostReduction(gameData, exileCardId, playerId, genericCostReduction);
        if (EffectResolution.needsTarget(card)) {
            List<UUID> candidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, playerId);
            boolean legalTargets = card.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, playerId)
                    : !candidates.isEmpty();
            if (!legalTargets) {
                gameData.exilePlayCostModifiers.remove(exileCardId);
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " has no valid targets and stays exiled."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    card, playerId, spellEffects, spellType, false, List.of(), genericCostReduction));
            playerInputService.beginPermanentChoice(gameData, playerId, candidates,
                    "Choose a target for " + card.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(gameData, player, exileCardId, 0,
                    (UUID) null);
        } catch (IllegalStateException ex) {
            gameData.exilePlayCostModifiers.remove(exileCardId);
            log.info("Game {} - {} could not pay the reduced cost for {}", gameData.id,
                    player.getUsername(), card.getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void installCostReduction(GameData gameData, UUID cardId, UUID playerId, int amount) {
        gameData.exilePlayCostModifiers.put(cardId, new ExilePlayCostModifier(playerId, null, -amount));
    }
}
