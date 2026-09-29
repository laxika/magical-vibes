package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Pep's combat-damage exile, play permission, and perpetual artifact grant. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var pepEffect =
                (ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffect)
                        effect;
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null || controllerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        String damagedPlayerName = gameData.playerIdToName.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(damagedPlayerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, damagedPlayerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());

        if (isNonlandPermanent(topCard)) {
            recordPerpetualCharacteristics(gameData, topCard, pepEffect.characteristics());
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text(damagedPlayerName + " exiles ").card(topCard)
                .text(" from the top of their library — ")
                .text(controllerName + " may play it this turn.")
                .build());
    }

    private void recordPerpetualCharacteristics(GameData gameData, Card card,
                                                 PerpetuallyGrantCardCharacteristicsEffect grant) {
        UUID cardId = card.getId();
        gameData.perpetualCardTypes.merge(cardId, Set.copyOf(grant.cardTypes()),
                (existing, added) -> unionCardTypes(existing, added));
        gameData.perpetualCardSubtypes.merge(cardId, Set.copyOf(grant.subtypes()),
                (existing, added) -> unionCardSubtypes(existing, added));
        gameData.perpetualActivatedAbilities.compute(cardId, (ignored, existing) -> {
            List<com.github.laxika.magicalvibes.model.ActivatedAbility> updated =
                    new ArrayList<>(existing == null ? List.of() : existing);
            grant.activatedAbilities().forEach(ability -> {
                if (!updated.contains(ability)) {
                    updated.add(ability);
                }
            });
            return List.copyOf(updated);
        });
    }

    private static Set<CardType> unionCardTypes(Set<CardType> existing, Set<CardType> added) {
        EnumSet<CardType> merged = EnumSet.noneOf(CardType.class);
        merged.addAll(existing);
        merged.addAll(added);
        return Set.copyOf(merged);
    }

    private static Set<CardSubtype> unionCardSubtypes(Set<CardSubtype> existing, Set<CardSubtype> added) {
        EnumSet<CardSubtype> merged = EnumSet.noneOf(CardSubtype.class);
        merged.addAll(existing);
        merged.addAll(added);
        return Set.copyOf(merged);
    }

    private static boolean isNonlandPermanent(Card card) {
        if (card.hasType(CardType.LAND)) {
            return false;
        }
        if (card.getType() != null && card.getType().isPermanentType()) {
            return true;
        }
        return card.getAdditionalTypes().stream().anyMatch(CardType::isPermanentType);
    }
}
