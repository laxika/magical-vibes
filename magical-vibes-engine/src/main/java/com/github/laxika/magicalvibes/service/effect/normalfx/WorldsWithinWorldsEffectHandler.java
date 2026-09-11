package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.WorldsWithinWorldsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileAllPermanentsEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorldsWithinWorldsEffectHandler implements NormalEffectHandlerBean {

    private final ExileAllPermanentsEffectHandler exileAllPermanentsEffectHandler;
    private final WorldsWithinWorldsSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WorldsWithinWorldsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> creatureCardIds = creatureCardIds(gameData);
        exileAllPermanentsEffectHandler.resolve(gameData, entry,
                new ExileAllPermanentsEffect(new PermanentIsCreaturePredicate()));

        List<UUID> exiledCardIds = creatureCardIds.stream()
                .filter(cardId -> gameData.findExiledCard(cardId) != null)
                .toList();
        List<UUID> orderedPlayerIds = support.apnapOrder(gameData);
        boolean begunChoice = support.beginNextChoice(gameData, orderedPlayerIds,
                exiledCardIds, new java.util.LinkedHashMap<>(), entry.getCard().getName());
        if (!begunChoice) {
            support.finish(gameData, exiledCardIds, new java.util.LinkedHashMap<>(), entry.getCard().getName());
        }
    }

    private List<UUID> creatureCardIds(GameData gameData) {
        List<UUID> cardIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .map(permanent -> permanent.getCard().getId())
                .forEach(cardIds::add));
        return cardIds;
    }
}
