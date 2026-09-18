package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveUpToCountersFromAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveUpToCountersFromAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RemoveUpToCountersFromAllPermanentsEffect typedEffect =
                (RemoveUpToCountersFromAllPermanentsEffect) effect;
        entry.setEventValue(0);
        if (typedEffect.maxAmount() == 0) {
            return;
        }

        Map<String, UUID> permanentOptions = permanentOptions(gameData, typedEffect.counterType());
        if (!permanentOptions.isEmpty()) {
            beginChoice(gameData, entry, typedEffect.counterType(), typedEffect.maxAmount(), permanentOptions);
        }
    }

    public void beginChoice(GameData gameData, StackEntry entry, CounterType counterType, int remaining,
                            Map<String, UUID> permanentOptions) {
        playerInputService.beginRemoveUpToCountersFromAllPermanentsChoice(
                gameData, entry, counterType, remaining, permanentOptions);
    }

    public Map<String, UUID> permanentOptions(GameData gameData, CounterType counterType) {
        List<Permanent> eligiblePermanents = new ArrayList<>();
        List<UUID> controllerIds = new ArrayList<>(gameData.playerBattlefields.keySet());
        controllerIds.sort(Comparator.comparing(UUID::toString));
        for (UUID controllerId : controllerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (permanent.getCounterCount(counterType) > 0) {
                        eligiblePermanents.add(permanent);
                    }
                }
            }
        }

        Map<String, Integer> occurrencesByName = new HashMap<>();
        Map<String, Integer> totalsByName = new HashMap<>();
        for (Permanent permanent : eligiblePermanents) {
            totalsByName.merge(permanent.getCard().getName(), 1, Integer::sum);
        }

        Map<String, UUID> options = new LinkedHashMap<>();
        for (Permanent permanent : eligiblePermanents) {
            String name = permanent.getCard().getName();
            int occurrence = occurrencesByName.merge(name, 1, Integer::sum);
            String label = totalsByName.get(name) == 1
                    ? name
                    : name + " (" + occurrence + ")";
            if (options.containsKey(label)) {
                label = name + " (" + occurrence + " - "
                        + permanent.getId().toString().substring(0, 8) + ")";
            }
            options.put(label, permanent.getId());
        }
        return options;
    }
}
