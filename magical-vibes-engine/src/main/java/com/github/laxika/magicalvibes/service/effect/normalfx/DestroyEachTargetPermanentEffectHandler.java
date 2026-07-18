package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyEachTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEachTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyEachTargetPermanentEffect) effect;

        List<UUID> targets = entry.getTargetIds();
        if (targets.isEmpty() && entry.getTargetId() != null) {
            targets = List.of(entry.getTargetId());
        }

        // Count the permanents actually put into a graveyard this way (indestructible/regenerated
        // targets don't count) and snapshot it onto the entry as its event value, so a later effect
        // on the same entry can reference "that many" via an EventValue amount (Volcanic Eruption).
        int destroyed = 0;
        for (UUID targetId : targets) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            if (destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), destroy.cannotBeRegenerated())) {
                destroyed++;
            }
        }
        entry.setEventValue(destroyed);
    }
}
