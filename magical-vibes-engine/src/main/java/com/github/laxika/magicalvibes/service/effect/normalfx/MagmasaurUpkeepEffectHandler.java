package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MagmasaurUpkeepEffect;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "You may remove a +1/+1 counter from this creature. If you don't, sacrifice this creature and it
 * deals damage equal to the number of +1/+1 counters on it to each creature without flying and each
 * player." With no +1/+1 counter there is no choice to make, so the penalty applies without a prompt.
 * Resolving only reads before it opens the prompt or sacrifices, so a Magmasaur with competing death
 * replacements can be asked about before it leaves (CR 616.1).
 */
@Component
@RequiredArgsConstructor
public class MagmasaurUpkeepEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final MagmasaurUpkeepSupport magmasaurUpkeepSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MagmasaurUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        if (magmasaurUpkeepSupport.counters(gameData, entry.getSourcePermanentId()) <= 0) {
            magmasaurUpkeepSupport.applyPenalty(
                    gameData, controllerId, entry.getSourcePermanentId(), entry.getCard(),
                    entry.getSourcePermanentSnapshot());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(effect),
                entry.getCard().getName() + " - Remove a +1/+1 counter from it?",
                null, null, entry.getSourcePermanentId()));
    }
}
