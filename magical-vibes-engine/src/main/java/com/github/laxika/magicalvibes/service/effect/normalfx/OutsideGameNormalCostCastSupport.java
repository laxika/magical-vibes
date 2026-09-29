package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.OutsideGameCards;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Starts a normal-cost instant or sorcery cast from a sideboard during resolution. */
@Slf4j
@Component
public class OutsideGameNormalCostCastSupport {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final SpellCastingService spellCastingService;

    public OutsideGameNormalCostCastSupport(GameLogService gameLogService,
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

    public void castFromOutsideGameWithNormalCost(GameData gameData, Player player, UUID cardId) {
        Card card = OutsideGameCards.view(gameData, player.getId()).stream()
                .filter(candidate -> candidate.getId().equals(cardId))
                .findFirst()
                .orElse(null);
        if (card == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.outsideGamePlayPermissions.add(cardId);
        ChooseOneEffect modal = card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (modal != null) {
            int maximumChoices = Math.min(modal.options().size(), modal.choicesMax() + 1);
            if (modal.choicesRequired() > maximumChoices) {
                gameData.outsideGamePlayPermissions.remove(cardId);
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            gameData.outsideGameAdditionalModalModePermissions.add(cardId);
            Card runtimeCard = card.createRuntimeCopy();
            List<Integer> offeredModes = java.util.stream.IntStream.range(0, modal.options().size())
                    .boxed()
                    .toList();
            playerInputService.beginExileFreeCastModeChoice(
                    gameData, player.getId(), runtimeCard, modal,
                    exileCastTargetSupport.mapCardTypeToSpellType(card),
                    List.of(), offeredModes, maximumChoices, false, true);
            return;
        }
        if (EffectResolution.needsTarget(card)) {
            List<UUID> candidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, player.getId());
            boolean legalTargets = card.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, player.getId())
                    : !candidates.isEmpty();
            if (!legalTargets) {
                gameData.outsideGamePlayPermissions.remove(cardId);
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " has no valid targets and stays outside the game."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            List<com.github.laxika.magicalvibes.model.effect.CardEffect> spellEffects =
                    new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    card, player.getId(), spellEffects, spellType, false, List.of(), 0,
                    true, 0, false, true));
            playerInputService.beginPermanentChoice(gameData, player.getId(), candidates,
                    "Choose a target for " + card.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(gameData, player, cardId, 0, (UUID) null);
        } catch (IllegalStateException ex) {
            gameData.outsideGamePlayPermissions.remove(cardId);
            log.info("Game {} - {} could not pay the normal cost for {} from outside the game",
                    gameData.id, player.getUsername(), card.getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void castPreparedModalCard(GameData gameData, Player player,
                                      ChoiceContext.ExileFreeCastModeChoice context,
                                      List<Integer> chosenModeIndices) {
        Card card = context.cardToCast().createRuntimeCopy();
        List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        ChooseOneEffect modal = context.effect();
        ChooseOneEffect expandedModal = new ChooseOneEffect(
                modal.options(), modal.optional(), modal.choicesRequired(), context.maximumChoices(),
                modal.allModesWhenOptionalCostPaid(), modal.modesMayRepeat(), null,
                modal.modeCosts(), modal.modeBudget(), modal.choicesEqualModalXValue());
        int modalIndex = spellEffects.indexOf(modal);
        if (modalIndex < 0) {
            modalIndex = 0;
        }
        spellEffects.set(modalIndex, expandedModal);
        int modeEncoding = modal.modesMayRepeat()
                ? expandedModal.encodeRepeatedSelection(chosenModeIndices.stream().mapToInt(Integer::intValue).toArray())
                : ChooseOneEffect.encodeModeSelection(modal.choicesRequired(), context.maximumChoices(),
                chosenModeIndices.stream().mapToInt(Integer::intValue).toArray());
        spellCastingService.prepareModalSpellCast(gameData, player.getId(), card, spellEffects, modeEncoding);

        boolean needsTarget = EffectResolution.needsSpellCastTarget(
                spellEffects, card.isAuraThatRequiresAttachment(), card.isEnchantPlayer())
                || EffectResolution.needsSpellTarget(spellEffects);
        if (needsTarget) {
            List<UUID> candidates = exileCastTargetSupport.firstSlotCandidates(
                    gameData, card, spellEffects, player.getId());
            boolean legalTargets = card.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, player.getId())
                    : !candidates.isEmpty();
            if (!legalTargets) {
                gameData.outsideGamePlayPermissions.remove(card.getId());
                gameData.outsideGameAdditionalModalModePermissions.remove(card.getId());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    card, player.getId(), spellEffects,
                    exileCastTargetSupport.mapCardTypeToSpellType(card), false, List.of(), 0,
                    true, 0, false, true, modeEncoding));
            playerInputService.beginPermanentChoice(gameData, player.getId(), candidates,
                    "Choose a target for " + card.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(
                    gameData, player, card.getId(), modeEncoding, (UUID) null);
        } catch (IllegalStateException ex) {
            gameData.outsideGamePlayPermissions.remove(card.getId());
            gameData.outsideGameAdditionalModalModePermissions.remove(card.getId());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
