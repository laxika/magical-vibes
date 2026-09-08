package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnlyEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SacrificeOnlyTriggerCollectorService {

    @CollectsTrigger(value = SacrificeOnlyEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handle(TriggerMatchContext match, SacrificeOnlyEffect effect, TriggerContext context) {
        if (!(context instanceof TriggerContext.SelfDeath selfDeath)) {
            return false;
        }

        Card sourceCard = selfDeath.dyingCard();
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                selfDeath.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect))));
        log.info("Game {} - {}'s sacrifice-only trigger fires", match.gameData().id, sourceCard.getName());
        return true;
    }
}
