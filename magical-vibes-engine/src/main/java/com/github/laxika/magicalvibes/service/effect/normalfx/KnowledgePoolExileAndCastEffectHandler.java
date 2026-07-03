package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgePoolExileAndCastEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KnowledgePoolExileAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (KnowledgePoolExileAndCastEffect) effect;
        UUID kpPermanentId = e.knowledgePoolPermanentId();

        // Step 1: Verify KP permanent still on battlefield
        Permanent kpPermanent = gameQueryService.findPermanentById(gameData, kpPermanentId);
        if (kpPermanent == null) {
            log.info("Game {} - Knowledge Pool no longer on battlefield, trigger fizzles", gameData.id);
            return;
        }

        // Step 2: Find original spell on stack by card ID
        UUID originalSpellCardId = e.originalSpellCardId();
        StackEntry originalSpell = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(originalSpellCardId)) {
                originalSpell = se;
                break;
            }
        }

        if (originalSpell == null) {
            // "If the player does" fails — original spell already gone (countered or exiled by another KP)
            log.info("Game {} - Original spell no longer on stack, Knowledge Pool 'if the player does' fails", gameData.id);
            String logEntry = "Knowledge Pool's ability — original spell is no longer on the stack.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Step 3: Remove original spell from stack, add to KP pool + player exile
        // "that player" = the player who cast the original spell (not the KP controller)
        UUID castingPlayerId = e.castingPlayerId();
        Card originalCard = originalSpell.getCard();
        gameData.stack.remove(originalSpell);

        exileService.exileCard(gameData, castingPlayerId, originalCard, kpPermanentId);

        String playerName = gameData.playerIdToName.get(castingPlayerId);
        String exileLog = playerName + " exiles " + originalCard.getName() + " (Knowledge Pool).";
        gameBroadcastService.logAndBroadcast(gameData, exileLog);
        log.info("Game {} - {} exiles {} to Knowledge Pool", gameData.id, playerName, originalCard.getName());

        // Step 4: Collect eligible cards — nonland, not the just-exiled card, from KP's pool
        List<Card> eligible = gameData.getCardsExiledByPermanent(kpPermanentId).stream()
                .filter(c -> !c.getId().equals(originalCard.getId()))
                .filter(c -> !c.hasType(CardType.LAND))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            String noChoiceLog = "Knowledge Pool — no other nonland cards exiled. " + playerName + " cannot cast a spell.";
            gameBroadcastService.logAndBroadcast(gameData, noChoiceLog);
            log.info("Game {} - No eligible cards in Knowledge Pool for {}", gameData.id, playerName);
            return;
        }

        // Step 5: Present choice to the player
        gameData.queueInteraction(new com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast(kpPermanentId));

        List<UUID> validCardIds = eligible.stream().map(Card::getId).toList();
        List<CardView> cardViews = eligible.stream().map(cardViewFactory::create).toList();

        gameData.interaction.beginKnowledgePoolCastChoice(castingPlayerId, new java.util.HashSet<>(validCardIds), 1);
        playerInputService.sendKnowledgePoolCastChoice(gameData, castingPlayerId, validCardIds, cardViews);
    }
}
