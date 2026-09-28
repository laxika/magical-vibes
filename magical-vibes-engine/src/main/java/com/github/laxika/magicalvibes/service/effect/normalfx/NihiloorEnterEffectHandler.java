package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NihiloorEnterEffect;
import com.github.laxika.magicalvibes.model.effect.NihiloorTapAndStealEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Expands Nihiloor's ETB into one sequential choice for each opponent. */
@Component
public class NihiloorEnterEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NihiloorEnterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardEffect> opponentEffects = apnapOpponents(gameData, entry.getControllerId()).stream()
                .map(NihiloorTapAndStealEffect::new)
                .map(cardEffect -> (CardEffect) cardEffect)
                .toList();
        if (!opponentEffects.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, opponentEffects);
        }
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        return rotated.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
