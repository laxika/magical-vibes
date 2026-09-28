package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Vodalian Tide Mage's choice among the creatures from the triggering damage event. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect) effect;
        List<UUID> validIds = currentControlledDealerIds(gameData, entry.getControllerId(),
                conjure.combatDamageDealerIds());
        if (validIds.isEmpty()) {
            return;
        }
        if (validIds.size() == 1) {
            conjureDuplicate(gameData, entry.getControllerId(), validIds.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ConjureDuplicateOfCombatDamageDealerChoice(
                        entry.getControllerId(), entry.getCard(), validIds));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validIds,
                entry.getCard().getName() + " - Choose one of those creatures.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.ConjureDuplicateOfCombatDamageDealerChoice context) {
        if (!context.combatDamageDealerIds().contains(chosenPermanentId)
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, chosenPermanentId))) {
            return;
        }
        conjureDuplicate(gameData, context.controllerId(), chosenPermanentId);
    }

    private List<UUID> currentControlledDealerIds(GameData gameData, UUID controllerId, List<UUID> dealerIds) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> dealerIds.contains(permanent.getId()))
                .map(Permanent::getId)
                .toList();
    }

    private void conjureDuplicate(GameData gameData, UUID controllerId, UUID permanentId) {
        Permanent dealer = gameQueryService.findPermanentById(gameData, permanentId);
        if (dealer == null) {
            return;
        }

        Card duplicate = dealer.getCard().createRuntimeCopyWithNewId();
        duplicate.setOwnerId(controllerId);
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        duplicate.freeze();
        gameData.addCardToHand(controllerId, duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into your hand."));
    }
}
