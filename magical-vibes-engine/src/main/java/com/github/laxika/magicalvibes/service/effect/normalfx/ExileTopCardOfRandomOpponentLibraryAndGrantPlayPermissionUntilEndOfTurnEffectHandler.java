package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardIdPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves The Ruinous Powers' random-opponent top-card exile. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        UUID opponentId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        List<Card> library = gameData.playerDecks.get(opponentId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, opponentId, topCard, entry.getSourcePermanentId());
        gameData.grantExilePlayPermission(topCard.getId(), entry.getControllerId(), Duration.UNTIL_END_OF_TURN);
        if (!topCard.hasType(CardType.LAND)) {
            gameData.grantExileAnyManaType(topCard.getId(), Duration.UNTIL_END_OF_TURN);
        }
        // "When you cast a spell this way" is a delayed triggered ability this resolution creates (CR 603.7a), about the
        // exiled card: it triggers once, this turn, whether or not The Ruinous Powers is still on the battlefield.
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(),
                RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(null,
                                List.of(new LoseLifeToOwnerOfTriggeringSpellEffect()))
                        .withStackEntryFilter(new StackEntryCardIdPredicate(topCard.getId())).delayedAbility(),
                entry.getSourcePermanentId()));

        String opponentName = gameData.playerIdToName.getOrDefault(opponentId, "that player");
        gameLogService.append(gameData, GameLog.builder()
                .text(entry.getCard().getName() + " exiles ").card(topCard)
                .text(" from " + opponentName + "'s library (may play it this turn).")
                .build());
    }
}
