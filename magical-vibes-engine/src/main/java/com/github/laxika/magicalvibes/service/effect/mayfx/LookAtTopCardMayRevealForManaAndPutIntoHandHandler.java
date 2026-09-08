package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealForManaAndPutIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealForManaAndPutIntoHandHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealForManaAndPutIntoHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealForManaAndPutIntoHandEffect effect = ability.effects().stream()
                .filter(LookAtTopCardMayRevealForManaAndPutIntoHandEffect.class::isInstance)
                .map(LookAtTopCardMayRevealForManaAndPutIntoHandEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null || effect.stage() != LookAtTopCardMayRevealForManaAndPutIntoHandEffect.Stage.MAY_REVEAL) {
            return;
        }

        UUID controllerId = ability.controllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card topCard = deck.removeFirst();
        if (accepted) {
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " reveals ")
                    .card(topCard)
                    .text(" and puts it into their hand.")
                    .build());
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " puts the top card of their library into their hand."));
        }
        gameData.addCardToHand(controllerId, topCard);

        boolean fromCreature = sourceIsCreature(gameData);
        if (!accepted || effect.colors().size() == 0) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (effect.colors().size() == 1) {
            addMana(gameData, controllerId, effect.colors().getFirst(), 3, fromCreature);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        ChoiceContext.ManaColorChoice choiceContext = ChoiceContext.ManaColorChoice
                .fixedColorCombination(controllerId, fromCreature, 3, effect.colors());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId,
                null,
                null,
                choiceContext,
                effect.colors().stream().map(Enum::name).toList(),
                "Choose a color of mana to add."));
    }

    private boolean sourceIsCreature(GameData gameData) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null || entry.getSourcePermanentId() == null) {
            return false;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null && gameQueryService.isCreature(gameData, source);
    }

    private static void addMana(GameData gameData, UUID playerId, ManaColor color, int amount,
                                boolean fromCreature) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        pool.add(color, amount);
        if (fromCreature) {
            pool.addCreatureMana(color, amount);
        }
    }
}
