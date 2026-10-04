package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExilePlayDuration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandCardExiledWithSourceMayCastEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseNonlandCardExiledWithSourceMayCastEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNonlandCardExiledWithSourceMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<UUID> eligibleCardIds = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> entry.getResolutionExiledCardIds().contains(card.getId()))
                .filter(card -> !card.hasType(CardType.LAND))
                .map(card -> card.getId())
                .toList();
        if (eligibleCardIds.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        if (eligibleCardIds.size() == 1) {
            grantPermission(gameData, eligibleCardIds.getFirst(), controllerId);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExiledCardMayPlayChoice(
                controllerId, eligibleCardIds, ExilePlayDuration.WHILE_EXILED, true));
    }

    private void grantPermission(GameData gameData, UUID cardId, UUID controllerId) {
        exileSupport.grantPlayWhileExiled(gameData, cardId, controllerId);
        gameData.exilePlayAnyManaTypeWhileExiled.add(cardId);
    }
}
