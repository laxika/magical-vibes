package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CreateTokenEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes the optional land entry and creates a Goblin when the land was not put in. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenHandler
        implements MayEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect.class::isInstance)
                .map(LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect.class::cast)
                .findFirst()
                .orElseThrow();
        List<Card> deck = gameData.playerDecks.get(player.getId());

        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            Permanent permanent = new Permanent(topCard, Zone.LIBRARY);
            if (effect.enterTapped()) {
                permanent.tap();
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), permanent);
            if (topCard.hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, player.getId(), topCard, null, false);
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", topCard, " onto the battlefield."));
        } else {
            createFallbackToken(gameData, ability, effect);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void createFallbackToken(GameData gameData, PendingMayAbility ability,
                                     LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect effect) {
        StackEntry tokenEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                ability.controllerId(),
                ability.sourceCard().getName() + "'s ability",
                List.of(effect.fallbackToken()),
                null,
                ability.sourcePermanentId());
        createTokenEffectHandler.resolveForController(
                gameData, tokenEntry, effect.fallbackToken(), ability.controllerId());
    }
}
