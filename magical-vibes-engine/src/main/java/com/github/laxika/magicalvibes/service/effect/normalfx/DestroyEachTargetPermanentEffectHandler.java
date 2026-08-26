package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        // A single spell or ability destroys all of its targets simultaneously, so route the whole
        // batch through destroyBatchCollecting rather than destroying one at a time — otherwise the
        // creatures don't see each other die and "whenever a creature dies" triggers under-count.
        List<Permanent> toDestroy = new ArrayList<>();
        Map<UUID, UUID> controllerByPermanentId = new HashMap<>();
        HashSet<UUID> seenTargetIds = new HashSet<>();
        for (UUID targetId : targets) {
            if (!seenTargetIds.add(targetId)) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            toDestroy.add(target);
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                controllerByPermanentId.put(target.getId(), controllerId);
            }
        }

        List<Permanent> actuallyDestroyed = destructionSupport.destroyBatchCollecting(
                gameData, toDestroy, entry.getCard().getName(), destroy.cannotBeRegenerated());

        List<UUID> destroyedControllerIds = new ArrayList<>();
        for (Permanent perm : actuallyDestroyed) {
            UUID controllerId = controllerByPermanentId.get(perm.getId());
            if (controllerId != null) {
                destroyedControllerIds.add(controllerId);
            }
        }
        entry.setEventValue(actuallyDestroyed.size());
        // Per-permanent controller tally for riders that need "the number of permanents THEY
        // controlled that were put into a graveyard this way" (Builder's Bane), which the single
        // event value can't express. Duplicates are meaningful: three artifacts lost = three entries.
        entry.setEventPlayerIds(destroyedControllerIds);
    }
}
