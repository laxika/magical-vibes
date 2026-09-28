package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CloneOperationState;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInExileOnEnterEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Prepares The Master's optional copy replacement before its permanent enters the battlefield. */
@Component
@RequiredArgsConstructor
public class ExiledCreatureCopyOnEnterService {

    private final PlayerInputService playerInputService;

    public boolean prepare(GameData gameData, UUID controllerId, Card card, UUID targetId, int xValue,
                           Card physicalCard, boolean transformed) {
        CopyCreatureCardInExileOnEnterEffect copyEffect = findCopyEffect(card);
        if (copyEffect == null) {
            return false;
        }

        List<ExiledCardEntry> eligible = gameData.exiledCards.stream()
                .filter(entry -> !entry.faceDown())
                .filter(entry -> entry.card().hasType(CardType.CREATURE))
                .filter(entry -> gameData.exiledCardsWithTakeoverCounters.contains(entry.card().getId()))
                .toList();
        if (eligible.isEmpty()) {
            return false;
        }

        CloneOperationState operation = gameData.cloneOperation;
        operation.card = card;
        operation.physicalCard = physicalCard;
        operation.transformed = transformed;
        operation.controllerId = controllerId;
        operation.etbTargetId = targetId;
        operation.powerOverride = null;
        operation.toughnessOverride = null;
        operation.copyPowerToughnessFromSource = false;
        operation.nameOverride = null;
        operation.addTypeAppropriateCounters = false;
        operation.embalmColorOverride = null;
        operation.embalmAddedSubtype = null;
        operation.embalmRemoveManaCost = false;
        operation.additionalPlusOnePlusOneCounters = null;
        operation.additionalCreatureOnlyCharacteristics = false;
        operation.additionalTypesOverride = java.util.Set.of();
        operation.additionalActivatedAbilities = List.of();
        operation.additionalSupertypesOverride = java.util.Set.of();
        operation.removedSupertypesOverride = java.util.Set.of();
        operation.additionalKeywordsOverride = java.util.Set.of();
        operation.additionalColorsOverride = java.util.Set.of();
        operation.additionalSubtypesOverride = java.util.Set.of();
        operation.additionalSlotEffects = java.util.Map.of();
        operation.shieldCounterIfControllerControlsCopiedPermanent = false;
        operation.copyColor = true;
        operation.copyUntilEndOfTurn = false;
        operation.addVanishingIfCopiedPermanentLacksIt = false;
        operation.entersTapped = false;
        operation.ninjutsuEntry = false;
        operation.ninjutsuAttackTargetId = null;
        operation.landPlay = false;
        operation.xValue = xValue;
        operation.copyCardFilter = null;
        operation.graveyardCopyChoicePending = false;
        operation.exileCopiedGraveyardCardAfterEntry = false;
        operation.mimeoplasmGraveyardChoicePending = false;
        operation.mimeoplasmCopyChoicePending = false;
        operation.mimeoplasmSelectedCardIds = List.of();
        operation.exileTwoAndAddOtherPowerCounters = false;
        operation.selectedGraveyardCopyCardIds = List.of();

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                card,
                controllerId,
                List.of(copyEffect),
                card.getName() + " — You may have it enter as a copy of a creature card in exile with a takeover counter."
        ));
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    private CopyCreatureCardInExileOnEnterEffect findCopyEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof CopyCreatureCardInExileOnEnterEffect copyEffect) {
                return copyEffect;
            }
        }
        return null;
    }
}
