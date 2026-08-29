package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class VaanExileCastSupport {

    private final ExileCastTargetSupport exileCastTargetSupport;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final SpellCastingService spellCastingService;

    public VaanExileCastSupport(ExileCastTargetSupport exileCastTargetSupport,
                                CreateTokenEffectHandler createTokenEffectHandler,
                                GameLogService gameLogService,
                                @Lazy PlayerInputService playerInputService,
                                @Lazy InputCompletionService inputCompletionService,
                                @Lazy SpellCastingService spellCastingService) {
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.createTokenEffectHandler = createTokenEffectHandler;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.inputCompletionService = inputCompletionService;
        this.spellCastingService = spellCastingService;
    }

    public void beginCast(GameData gameData, Player player, Card sourceCard, UUID exileCardId,
                          UUID sourcePermanentId) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null || exiledEntry.card().hasType(CardType.LAND)) {
            createTreasureDuringResolution(gameData, player.getId(), sourceCard, sourcePermanentId);
            return;
        }

        Card card = exiledEntry.card();
        if (EffectResolution.needsTarget(card)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, player.getId());
            boolean hasLegalTargets = card.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, player.getId())
                    : !firstCandidates.isEmpty();
            if (!hasLegalTargets) {
                createTreasureDuringResolution(gameData, player.getId(), sourceCard, sourcePermanentId);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.VaanCastSpellTarget(
                    card, player.getId(), sourceCard, sourcePermanentId, List.of()));
            playerInputService.beginPermanentChoice(gameData, player.getId(), firstCandidates,
                    "Choose a target for " + card.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(gameData, player, card.getId(), 0,
                    (UUID) null);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } catch (IllegalStateException ex) {
            log.info("Game {} - {} could not cast {} from Vaan's exile ability",
                    gameData.id, player.getUsername(), card.getName());
            createTreasure(gameData, player.getId(), sourceCard, sourcePermanentId);
        }
    }

    public void completeTarget(GameData gameData, UUID targetId,
                               PermanentChoiceContext.VaanCastSpellTarget context) {
        Card card = context.cardToCast();
        if (card.getMaxTargets() > 1) {
            List<UUID> chosen = new ArrayList<>(context.chosenTargets());
            chosen.add(targetId);
            if (chosen.size() < card.getMaxTargets()) {
                List<UUID> nextCandidates = exileCastTargetSupport.nextSlotCandidates(
                        gameData, card, context.controllerId(), chosen);
                if (nextCandidates.isEmpty()) {
                    createTreasure(gameData, context.controllerId(), context.sourceCard(), context.sourcePermanentId());
                    return;
                }
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.VaanCastSpellTarget(
                        card, context.controllerId(), context.sourceCard(), context.sourcePermanentId(), chosen));
                playerInputService.beginPermanentChoice(gameData, context.controllerId(), nextCandidates,
                        "Choose a target for " + card.getName() + ".");
                return;
            }

            try {
                Player player = playerFor(gameData, context.controllerId());
                spellCastingService.playCardFromExileAsResolutionCast(gameData, player, card.getId(), 0, chosen);
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            } catch (IllegalStateException ex) {
                createTreasure(gameData, context.controllerId(), context.sourceCard(), context.sourcePermanentId());
            }
            return;
        }

        try {
            Player player = playerFor(gameData, context.controllerId());
            spellCastingService.playCardFromExileAsResolutionCast(gameData, player, card.getId(), 0, targetId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } catch (IllegalStateException ex) {
            createTreasure(gameData, context.controllerId(), context.sourceCard(), context.sourcePermanentId());
        }
    }

    public void createTreasure(GameData gameData, UUID controllerId, Card sourceCard, UUID sourcePermanentId) {
        createTreasureDuringResolution(gameData, controllerId, sourceCard, sourcePermanentId);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void createTreasureDuringResolution(GameData gameData, UUID controllerId,
                                                Card sourceCard, UUID sourcePermanentId) {
        CreateTokenEffect treasure = CreateTokenEffect.ofTreasureToken(1);
        StackEntry treasureEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + " creates a Treasure token.",
                List.of(treasure),
                (UUID) null,
                sourcePermanentId
        );
        createTokenEffectHandler.resolve(gameData, treasureEntry, treasure);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " creates a Treasure token."));
    }

    private Player playerFor(GameData gameData, UUID playerId) {
        return new Player(playerId, gameData.playerIdToName.get(playerId));
    }
}
