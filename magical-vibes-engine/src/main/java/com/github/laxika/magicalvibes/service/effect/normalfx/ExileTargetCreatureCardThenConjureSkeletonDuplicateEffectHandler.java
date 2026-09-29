package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardThenConjureSkeletonDuplicateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Legion Reconsecrator's graveyard exile and modified duplicate. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardThenConjureSkeletonDuplicateEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardThenConjureSkeletonDuplicateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetCardIds() == null || entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId()
                : entry.getTargetCardIds().getFirst();
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || !targetCard.hasType(CardType.CREATURE)) {
            return;
        }
        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            return;
        }

        Card duplicate = targetCard.createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());

        List<CardColor> colors = new ArrayList<>(duplicate.getColors());
        if (duplicate.getColors().isEmpty() && duplicate.getColor() != null) {
            colors.add(duplicate.getColor());
        }
        if (!colors.contains(CardColor.BLACK)) {
            colors.add(CardColor.BLACK);
        }
        duplicate.setColors(List.copyOf(colors));

        List<CardSubtype> subtypes = new ArrayList<>(duplicate.getSubtypes());
        if (!subtypes.contains(CardSubtype.SKELETON)) {
            subtypes.add(CardSubtype.SKELETON);
        }
        duplicate.setSubtypes(List.copyOf(subtypes));
        duplicate.setPower(3);
        duplicate.setToughness(1);
        duplicate.freeze();

        graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A modified duplicate of ", targetCard, " is conjured into your graveyard."));
    }
}
