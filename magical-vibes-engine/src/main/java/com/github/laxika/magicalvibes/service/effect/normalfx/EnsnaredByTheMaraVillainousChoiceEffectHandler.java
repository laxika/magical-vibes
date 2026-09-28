package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EnsnaredByTheMaraVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Ensnared by the Mara's opponent-by-opponent villainous choice. */
@Component
@RequiredArgsConstructor
public class EnsnaredByTheMaraVillainousChoiceEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final DealDamageToPlayersEffectHandler damageHandler;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnsnaredByTheMaraVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(EachPlayerMayScryEffectHandler
                    .apnapOpponents(gameData, entry.getControllerId()));
        }

        if (state.waitingForControllerMay) {
            state.waitingForControllerMay = false;
            advance(gameData, entry);
            return;
        }

        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION.equals(chosen)) {
                exileUntilNonlandAndOfferCast(gameData, entry, state.currentPlayerId);
            } else {
                exileFourAndDealDamage(gameData, entry, state.currentPlayerId);
            }
            return;
        }

        advance(gameData, entry);
    }

    private void advance(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        UUID opponentId = state.remaining.pollFirst();
        if (opponentId == null) {
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        state.currentPlayerId = opponentId;
        gameData.rerunCurrentEffectAfterInteraction = true;
        villainousChoiceSupport.beginChoice(gameData, opponentId, entry.getCard().getName(),
                EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION,
                List.of(EnsnaredByTheMaraVillainousChoiceEffect.CAST_OPTION,
                        EnsnaredByTheMaraVillainousChoiceEffect.DAMAGE_OPTION),
                entry.getCard().getName() + " — Choose a villainous choice.");
    }

    private void exileUntilNonlandAndOfferCast(GameData gameData, StackEntry entry, UUID opponentId) {
        if (opponentId == null) {
            advance(gameData, entry);
            return;
        }

        List<Card> library = gameData.playerDecks.get(opponentId);
        Card nonland = null;
        while (library != null && !library.isEmpty()) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, opponentId, card);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(opponentId) + " exiles ")
                    .card(card).text(" from the top of their library.").build());
            if (!card.hasType(CardType.LAND)) {
                nonland = card;
                break;
            }
        }

        if (nonland == null) {
            advance(gameData, entry);
            return;
        }

        VillainousChoiceState state = gameData.villainousChoice;
        state.waitingForControllerMay = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + nonland.getName() + " without paying its mana cost?",
                nonland.getId()));
    }

    private void exileFourAndDealDamage(GameData gameData, StackEntry entry, UUID opponentId) {
        if (opponentId == null) {
            advance(gameData, entry);
            return;
        }

        List<Card> library = gameData.playerDecks.get(opponentId);
        int totalManaValue = 0;
        for (int i = 0; i < 4 && library != null && !library.isEmpty(); i++) {
            Card card = library.removeFirst();
            totalManaValue += card.getManaValue();
            exileService.exileCard(gameData, opponentId, card);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(opponentId) + " exiles ")
                    .card(card).text(" from the top of their library.").build());
        }

        if (totalManaValue > 0) {
            StackEntry damageEntry = new StackEntry(
                    entry.getEntryType(), entry.getCard(), entry.getControllerId(), entry.getDescription(),
                    List.of(), opponentId, entry.getSourcePermanentId());
            damageHandler.resolve(gameData, damageEntry,
                    new DealDamageToPlayersEffect(totalManaValue, DamageRecipient.TARGET_PLAYER));
        }
        advance(gameData, entry);
    }
}
