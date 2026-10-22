package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithCostReductionEffect;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
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

/**
 * Supports a may-cast-from-exile offer that pays the reduced mana cost. A split card is cast through
 * {@link SplitCardCastSupport}, so its half is chosen as it is cast and only that half's cost is paid.
 */
@Slf4j
@Component
public class ExileReducedCastSupport {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final SpellCastingService spellCastingService;
    private final SplitCardCastSupport splitCardCastSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final CastingCostService castingCostService;

    public ExileReducedCastSupport(GameLogService gameLogService,
                                   @Lazy PlayerInputService playerInputService,
                                   @Lazy InputCompletionService inputCompletionService,
                                   ExileCastTargetSupport exileCastTargetSupport,
                                   @Lazy SpellCastingService spellCastingService,
                                   @Lazy SplitCardCastSupport splitCardCastSupport,
                                   @Lazy InteractionHandlerRegistry interactionHandlerRegistry,
                                   @Lazy PotentialManaService potentialManaService,
                                   @Lazy CastingCostService castingCostService) {
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.inputCompletionService = inputCompletionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.spellCastingService = spellCastingService;
        this.splitCardCastSupport = splitCardCastSupport;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.potentialManaService = potentialManaService;
        this.castingCostService = castingCostService;
    }

    public void castFromExileWithCostReduction(GameData gameData, Player player, UUID exileCardId,
                                               int genericCostReduction) {
        castFromExileWithCostReduction(gameData, player, exileCardId, genericCostReduction, false);
    }

    public void castFromExileWithNormalCostDuringResolution(GameData gameData, Player player,
                                                             UUID exileCardId) {
        castFromExileWithCostReduction(gameData, player, exileCardId, 0, true);
    }

    public void castFromExileWithCostReduction(GameData gameData, Player player, UUID exileCardId,
                                               int genericCostReduction,
                                               boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard) {
        castFromExileWithCostReduction(gameData, player, exileCardId, genericCostReduction,
                putOnBottomOfOwnersLibraryInsteadOfGraveyard, null);
    }

    /** Resumes a reduced-cost cast after the controller chose the value of X for the spell. */
    public void resumeCastWithX(GameData gameData, Player player, UUID exileCardId,
                                int genericCostReduction, int chosenX) {
        castFromExileWithCostReduction(gameData, player, exileCardId, genericCostReduction, false, chosenX);
    }

    private void castFromExileWithCostReduction(GameData gameData, Player player, UUID exileCardId,
                                                int genericCostReduction,
                                                boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard,
                                                Integer chosenX) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card card = exiledEntry.card();
        UUID playerId = player.getId();
        installCostReduction(gameData, exileCardId, playerId, genericCostReduction);
        if (SplitCardCastSupport.isSplitCard(card)) {
            SplitCardCast.ExileCast exileCast = SplitCardCast.ExileCast.ofCard()
                    .puttingOnLibraryBottom(putOnBottomOfOwnersLibraryInsteadOfGraveyard);
            if (!splitCardCastSupport.beginFromExile(gameData, playerId, card, true, exileCast,
                    Integer.MAX_VALUE, null, null)) {
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " has no half that can be cast and stays exiled."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }
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
                    card, playerId, spellEffects, spellType, false, List.of(), genericCostReduction,
                    putOnBottomOfOwnersLibraryInsteadOfGraveyard, 0,
                    putOnBottomOfOwnersLibraryInsteadOfGraveyard));
            playerInputService.beginPermanentChoice(gameData, playerId, candidates,
                    "Choose a target for " + card.getName() + ".");
            return;
        }

        if (chosenX == null && genericCostReduction > 0
                && card.getParsedManaCost() != null && card.getParsedManaCost().hasX()) {
            beginXValueChoice(gameData, player, exileCardId, card, genericCostReduction);
            return;
        }
        try {
            spellCastingService.playCardFromExileAsResolutionCast(gameData, player, exileCardId,
                    chosenX == null ? 0 : chosenX, (UUID) null, false, putOnBottomOfOwnersLibraryInsteadOfGraveyard);
        } catch (IllegalStateException ex) {
            gameData.exilePlayCostModifiers.remove(exileCardId);
            log.info("Game {} - {} could not pay the {} for {}", gameData.id,
                    player.getUsername(), genericCostReduction > 0 ? "reduced cost" : "normal cost",
                    card.getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void beginXValueChoice(GameData gameData, Player player, UUID exileCardId, Card card,
                                   int genericCostReduction) {
        int tax = castingCostService.getCastCostModifier(gameData, player.getId(), card, 0, Zone.EXILE);
        int maxX = card.getParsedManaCost().calculateMaxX(
                potentialManaService.buildVirtualManaPool(gameData, player.getId()), tax);
        PendingMayAbility ability = new PendingMayAbility(card, player.getId(),
                List.<CardEffect>of(new MayCastExiledCardWithCostReductionEffect(genericCostReduction)),
                "Cast " + card.getName() + " for {" + genericCostReduction + "} less?", exileCardId, (UUID) null);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                player.getId(), 0, Math.max(0, maxX), "Choose a value for X to cast " + card.getName() + ".",
                card.getName(), true, card.getManaCost(), ability));
    }

    private void installCostReduction(GameData gameData, UUID cardId, UUID playerId, int amount) {
        gameData.exilePlayCostModifiers.put(cardId, new ExilePlayCostModifier(playerId, null, -amount));
    }
}
