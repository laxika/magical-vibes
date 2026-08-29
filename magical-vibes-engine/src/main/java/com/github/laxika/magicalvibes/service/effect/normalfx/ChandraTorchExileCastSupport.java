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
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
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
public class ChandraTorchExileCastSupport {

    private final ExileCastTargetSupport exileCastTargetSupport;
    private final GameLogService gameLogService;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final SpellCastingService spellCastingService;

    public ChandraTorchExileCastSupport(ExileCastTargetSupport exileCastTargetSupport,
                                        GameLogService gameLogService,
                                        DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler,
                                        @Lazy PlayerInputService playerInputService,
                                        @Lazy InputCompletionService inputCompletionService,
                                        @Lazy SpellCastingService spellCastingService) {
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.gameLogService = gameLogService;
        this.dealDamageToPlayersEffectHandler = dealDamageToPlayersEffectHandler;
        this.playerInputService = playerInputService;
        this.inputCompletionService = inputCompletionService;
        this.spellCastingService = spellCastingService;
    }

    public void beginCast(GameData gameData, Player player, Card sourceCard, UUID exileCardId, int damage) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null || exiledEntry.card().hasType(CardType.LAND)) {
            dealDamage(gameData, player.getId(), sourceCard, damage);
            return;
        }

        Card card = exiledEntry.card();
        if (EffectResolution.needsTarget(card)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, player.getId());
            boolean hasLegalTargets = card.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, player.getId())
                    : !firstCandidates.isEmpty();
            if (!hasLegalTargets) {
                dealDamage(gameData, player.getId(), sourceCard, damage);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChandraTorchCastSpellTarget(
                    card, player.getId(), sourceCard, damage, List.of()));
            playerInputService.beginPermanentChoice(gameData, player.getId(), firstCandidates,
                    "Choose a target for " + card.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(gameData, player, card.getId(), 0,
                    (UUID) null);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } catch (IllegalStateException ex) {
            log.info("Game {} - {} could not cast {} from Chandra's exile ability",
                    gameData.id, player.getUsername(), card.getName());
            dealDamage(gameData, player.getId(), sourceCard, damage);
        }
    }

    public void completeTarget(GameData gameData, UUID targetId,
                               PermanentChoiceContext.ChandraTorchCastSpellTarget context) {
        Card card = context.cardToCast();
        if (card.getMaxTargets() > 1) {
            List<UUID> chosen = new ArrayList<>(context.chosenTargets());
            chosen.add(targetId);
            if (chosen.size() < card.getMaxTargets()) {
                List<UUID> nextCandidates = exileCastTargetSupport.nextSlotCandidates(
                        gameData, card, context.controllerId(), chosen);
                if (nextCandidates.isEmpty()) {
                    dealDamage(gameData, context.controllerId(), context.sourceCard(), context.damage());
                    return;
                }
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChandraTorchCastSpellTarget(
                        card, context.controllerId(), context.sourceCard(), context.damage(), chosen));
                playerInputService.beginPermanentChoice(gameData, context.controllerId(), nextCandidates,
                        "Choose a target for " + card.getName() + ".");
                return;
            }

            try {
                Player player = playerFor(gameData, context.controllerId());
                spellCastingService.playCardFromExileAsResolutionCast(gameData, player, card.getId(), 0, chosen);
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            } catch (IllegalStateException ex) {
                dealDamage(gameData, context.controllerId(), context.sourceCard(), context.damage());
            }
            return;
        }

        try {
            Player player = playerFor(gameData, context.controllerId());
            spellCastingService.playCardFromExileAsResolutionCast(gameData, player, card.getId(), 0, targetId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } catch (IllegalStateException ex) {
            dealDamage(gameData, context.controllerId(), context.sourceCard(), context.damage());
        }
    }

    public void dealDamage(GameData gameData, UUID controllerId, Card sourceCard, int damage) {
        DealDamageToPlayersEffect damageEffect = new DealDamageToPlayersEffect(damage, DamageRecipient.EACH_OPPONENT);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + " deals " + damage + " damage to each opponent.",
                List.of(damageEffect),
                0,
                (UUID) null,
                null
        );
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damageEffect);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " deals " + damage + " damage to each opponent."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private Player playerFor(GameData gameData, UUID playerId) {
        return new Player(playerId, gameData.playerIdToName.get(playerId));
    }
}
