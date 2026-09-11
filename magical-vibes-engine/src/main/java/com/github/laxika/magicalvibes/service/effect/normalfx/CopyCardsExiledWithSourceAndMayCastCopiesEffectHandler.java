package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCardsExiledWithSourceAndMayCastCopiesEffect.CopyCastCost;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithNormalCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Copies one or all cards tracked as exiled by the source permanent and offers each copy. */
@Component
@RequiredArgsConstructor
public class CopyCardsExiledWithSourceAndMayCastCopiesEffectHandler
        implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyCardsExiledWithSourceAndMayCastCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyCardsExiledWithSourceAndMayCastCopiesEffect copyEffect =
                (CopyCardsExiledWithSourceAndMayCastCopiesEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Card> trackedCards = sourcePermanentId == null
                ? List.of()
                : gameData.getCardsExiledByPermanent(sourcePermanentId);
        List<Card> cardsToCopy = copyEffect.copyAll()
                ? trackedCards
                : singletonOrEmpty(findTargetCard(trackedCards, targetCardId(entry)));

        if (cardsToCopy.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no exiled card to copy."));
            return;
        }

        UUID controllerId = entry.getControllerId();
        for (int i = cardsToCopy.size() - 1; i >= 0; i--) {
            Card copy = copySupport.createCopyCard(cardsToCopy.get(i));
            exileService.exileCard(gameData, controllerId, copy);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    copy,
                    controllerId,
                    List.of(copyCastChoice(copyEffect.castCost())),
                    castDescription(copy, copyEffect.castCost()),
                    copy.getId()));
        }
    }

    private Card findTargetCard(List<Card> trackedCards, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        return trackedCards.stream()
                .filter(card -> card.getId().equals(targetCardId))
                .findFirst()
                .orElse(null);
    }

    private List<Card> singletonOrEmpty(Card card) {
        return card == null ? List.of() : List.of(card);
    }

    private UUID targetCardId(StackEntry entry) {
        if (entry.getTargetId() != null) {
            return entry.getTargetId();
        }
        List<UUID> targetCardIds = entry.getTargetCardIds();
        return targetCardIds == null || targetCardIds.isEmpty() ? null : targetCardIds.getFirst();
    }

    private CardEffect copyCastChoice(CopyCastCost castCost) {
        return switch (castCost) {
            case NORMAL -> new MayCastCopyWithNormalCostEffect();
            case ONE_GENERIC -> new MayCastCopyWithManaCostEffect("{1}");
            case FREE -> new MayCastCopyWithoutPayingManaCostEffect();
        };
    }

    private String castDescription(Card copy, CopyCastCost castCost) {
        return switch (castCost) {
            case NORMAL -> "Cast the copy of " + copy.getName() + " by paying its mana cost?";
            case ONE_GENERIC -> "Cast the copy of " + copy.getName() + " by paying {1}?";
            case FREE -> "Cast the copy of " + copy.getName() + " without paying its mana cost?";
        };
    }
}
