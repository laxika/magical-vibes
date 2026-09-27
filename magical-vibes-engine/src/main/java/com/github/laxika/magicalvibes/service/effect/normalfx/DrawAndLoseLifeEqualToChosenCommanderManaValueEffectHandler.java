package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifeEqualToChosenCommanderManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DrawAndLoseLifeEqualToChosenCommanderManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawAndLoseLifeEqualToChosenCommanderManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> commanders = eligibleCommanders(gameData, entry.getControllerId());
        if (commanders.isEmpty()) {
            return;
        }
        if (commanders.size() == 1) {
            insertDrawAndLifeLoss(gameData, entry, entry.getEffectsToResolve().indexOf(effect) + 1,
                    entry.getControllerId(), commanders.getFirst().getId());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.StingingStudyCommanderChoice(
                entry.getControllerId(), commanders));
    }

    public void completeChoice(GameData gameData,
                               PendingInteraction.StingingStudyCommanderChoice interaction,
                               UUID selectedCommanderId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }

        List<Card> commanders = eligibleCommanders(gameData, interaction.playerId());
        if (commanders.stream().noneMatch(card -> card.getId().equals(selectedCommanderId))) {
            throw new IllegalStateException("Selected commander is no longer eligible");
        }
        insertDrawAndLifeLoss(gameData, entry, gameData.pendingEffectResolutionIndex,
                interaction.playerId(), selectedCommanderId);
    }

    private void insertDrawAndLifeLoss(GameData gameData, StackEntry entry, int insertionIndex,
                                       UUID controllerId, UUID commanderId) {
        int manaValue = commanderManaValue(gameData, controllerId, commanderId);
        entry.setEventValue(manaValue);
        entry.insertEffectsToResolve(insertionIndex,
                List.of(new DrawCardEffect(new EventValue()), new LoseLifeEffect(manaValue)));
    }

    private List<Card> eligibleCommanders(GameData gameData, UUID ownerId) {
        List<Card> designatedCommanders = gameData.playerCommanders.getOrDefault(ownerId, List.of());
        Map<UUID, Card> commanders = new LinkedHashMap<>();
        for (Card card : gameData.playerCommandZones.getOrDefault(ownerId, List.of())) {
            if (designatedCommanders.stream().anyMatch(commander -> commander.getId().equals(card.getId()))) {
                commanders.putIfAbsent(card.getId(), card);
            }
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                UUID originalCardId = permanent.getOriginalCard().getId();
                if (designatedCommanders.stream().anyMatch(commander -> commander.getId().equals(originalCardId))) {
                    commanders.putIfAbsent(originalCardId, permanent.getCard());
                }
            }
        }
        return new ArrayList<>(commanders.values());
    }

    private int commanderManaValue(GameData gameData, UUID ownerId, UUID commanderId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getOriginalCard().getId().equals(commanderId)) {
                    return permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue();
                }
            }
        }
        return gameData.playerCommandZones.getOrDefault(ownerId, List.of()).stream()
                .filter(card -> card.getId().equals(commanderId))
                .mapToInt(Card::getManaValue)
                .findFirst()
                .orElse(0);
    }
}
