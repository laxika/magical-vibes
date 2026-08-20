package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves fixed-count top-library effects that offer any number of exiled spells for free. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsAndMayCastSpellsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsAndMayCastSpellsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsAndMayCastSpellsEffect) effect;
        int count = e.dynamicCount() == null
                ? e.count()
                : Math.max(0, amountEvaluationService.evaluate(gameData, e.dynamicCount(),
                        AmountContext.forStackEntry(entry, null)));
        if (count <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (e.trackWithSource() && sourcePermanentId == null) {
            return;
        }

        int manaValueLimit = e.manaValueLimit() == null
                ? Integer.MAX_VALUE
                : Math.max(0, amountEvaluationService.evaluate(gameData, e.manaValueLimit(),
                        AmountContext.forStackEntry(entry, null)));
        List<UUID> castableSpellIds = new ArrayList<>();

        for (UUID playerId : exilingPlayers(gameData, entry, e.scope(), controllerId)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            for (int i = 0; i < count && deck != null && !deck.isEmpty(); i++) {
                Card card = deck.removeFirst();
                if (e.trackWithSource()) {
                    exileService.exileCard(gameData, playerId, card, sourcePermanentId);
                } else {
                    gameData.addToExile(playerId, card);
                }
                gameLogService.append(gameData, GameLog.builder()
                        .text(playerName + " exiles ")
                        .card(card)
                        .text(" from the top of their library.")
                        .build());

                if (isSpell(card) && card.getManaValue() <= manaValueLimit) {
                    castableSpellIds.add(card.getId());
                }
            }
        }

        if (castableSpellIds.isEmpty()) {
            log.info("Game {} - {} found no spells among the exiled cards", gameData.id, entry.getCard().getName());
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, castableSpellIds.size()));
        log.info("Game {} - {} awaiting cast choices for {} exiled spells",
                gameData.id, entry.getCard().getName(), castableSpellIds.size());
    }

    private static List<UUID> exilingPlayers(GameData gameData, StackEntry entry,
                                             LibraryScope scope, UUID controllerId) {
        return switch (scope) {
            case CONTROLLER -> List.of(controllerId);
            case TARGET_PLAYER, TARGET_OPPONENT -> entry.getTargetId() != null
                    && gameData.orderedPlayerIds.contains(entry.getTargetId())
                    ? List.of(entry.getTargetId()) : List.of();
            case EACH_PLAYER -> List.copyOf(gameData.orderedPlayerIds);
        };
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
