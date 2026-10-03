package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentManaValuesDestroyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Running Is Useless's distinct-mana-value creature choice. */
@Component
@RequiredArgsConstructor
public class ChooseCreaturesWithDifferentManaValuesDestroyEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreaturesWithDifferentManaValuesDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> creatureIds = new ArrayList<>();
        gameData.forEachPermanent((ignoredControllerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        });

        if (creatureIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                creatureIds,
                creatureIds.size(),
                new MultiPermanentChoiceContext.ChooseCreaturesWithDifferentManaValuesDestroy(),
                "Choose any number of creatures with different mana values.");
    }

    public void validateChoice(GameData gameData, List<UUID> permanentIds) {
        Set<Integer> manaValues = new HashSet<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)) {
                throw new IllegalStateException("A selected permanent is no longer a creature");
            }
            if (!manaValues.add(manaValue(permanent))) {
                throw new IllegalStateException("Selected creatures must have different mana values");
            }
        }
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds, StackEntry entry) {
        validateChoice(gameData, permanentIds);
        List<Permanent> chosen = permanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .toList();
        destructionSupport.destroyBatch(gameData, chosen, entry.getCard().getName(), false);
    }

    private int manaValue(Permanent permanent) {
        return permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue();
    }
}
