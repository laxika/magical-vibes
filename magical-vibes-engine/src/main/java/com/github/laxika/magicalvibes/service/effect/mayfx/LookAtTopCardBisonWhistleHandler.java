package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardBisonWhistleEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles the optional Bison battlefield placement and chains its creature-card fallback. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardBisonWhistleHandler implements MayEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardBisonWhistleEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardBisonWhistleEffect effect = ability.effects().stream()
                .filter(LookAtTopCardBisonWhistleEffect.class::isInstance)
                .map(LookAtTopCardBisonWhistleEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (effect.stage() != LookAtTopCardBisonWhistleEffect.Stage.MAY_BATTLEFIELD) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card topCard = deck.getFirst();
        if (accepted) {
            putTopCardOntoBattlefield(gameData, player, topCard);
        } else if (topCard.hasType(CardType.CREATURE)) {
            queueCreatureHandChoice(gameData, ability, topCard);
        } else {
            queueGraveyardChoice(gameData, ability, topCard);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void putTopCardOntoBattlefield(GameData gameData, Player player, Card topCard) {
        UUID controllerId = player.getId();
        gameData.playerDecks.get(controllerId).removeFirst();
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, controllerId, new Permanent(topCard, Zone.LIBRARY));
        if (topCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, topCard, null, false);
        }
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " puts ", topCard, " onto the battlefield."));
        log.info("Game {} - {} puts {} onto the battlefield from library top (Bison Whistle)",
                gameData.id, player.getUsername(), topCard.getName());
    }

    private void queueCreatureHandChoice(GameData gameData, PendingMayAbility ability, Card topCard) {
        LookAtTopCardMayRevealMatchingToHandEffect handEffect =
                new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.TOP,
                        LookAtTopCardMayRevealMatchingToHandEffect.Stage.MAY_HAND);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                ability.sourceCard(), ability.controllerId(), List.of(handEffect),
                ability.sourceCard().getName() + " — Reveal " + topCard.getName()
                        + " and put it into your hand?"
        ));
    }

    private void queueGraveyardChoice(GameData gameData, PendingMayAbility ability, Card topCard) {
        LookAtTopCardMayRevealMatchingToHandEffect graveyardEffect =
                new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.GRAVEYARD,
                        LookAtTopCardMayRevealMatchingToHandEffect.Stage.MAY_GRAVEYARD);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                ability.sourceCard(), ability.controllerId(), List.of(graveyardEffect),
                ability.sourceCard().getName() + " — Put " + topCard.getName() + " into your graveyard?"
        ));
    }
}
