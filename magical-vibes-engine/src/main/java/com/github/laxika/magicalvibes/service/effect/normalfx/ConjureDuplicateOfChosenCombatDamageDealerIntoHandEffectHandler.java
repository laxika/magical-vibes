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
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect) effect;
        List<UUID> validIds = currentDealerIds(gameData, conjure.combatDamageDealerIds());
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
        if (!context.combatDamageDealerIds().contains(chosenPermanentId)) {
            return;
        }
        conjureDuplicate(gameData, context.controllerId(), chosenPermanentId);
    }

    private List<UUID> currentDealerIds(GameData gameData, List<UUID> dealerIds) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> dealerIds.contains(permanent.getId()))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private void conjureDuplicate(GameData gameData, UUID controllerId, UUID permanentId) {
        Permanent dealer = gameQueryService.findPermanentById(gameData, permanentId);
        if (dealer == null) {
            return;
        }

        Card duplicate = dealer.getCard().createConjuredCopy();
        duplicate.setOwnerId(controllerId);
        duplicate.freeze();
        gameData.addCardToHand(controllerId, duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into your hand."));
        triggerCollectionService.checkConjureTriggers(gameData, controllerId, 1);
    }
}
