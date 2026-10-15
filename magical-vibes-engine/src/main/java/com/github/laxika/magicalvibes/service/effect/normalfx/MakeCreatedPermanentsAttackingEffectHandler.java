package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Lets the controller choose a defender for each created attacking permanent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MakeCreatedPermanentsAttackingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    /**
     * Whether permanents this entry creates are put onto the battlefield "tapped and attacking", so
     * the creating handler must tap them before they enter (Amulet of Vigor sees them enter tapped).
     */
    public static boolean entersTappedAndAttacking(StackEntry entry) {
        return entry != null && entry.getEffectsToResolve().stream()
                .anyMatch(effect -> effect instanceof MakeCreatedPermanentsAttackingEffect makeAttacking
                        && makeAttacking.tapped());
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatedPermanentsAttackingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var makeAttacking = (MakeCreatedPermanentsAttackingEffect) effect;
        if (makeAttacking.tapped()) {
            // Creators that check entersTappedAndAttacking already put these onto the battlefield
            // tapped; this only covers the rest.
            for (var createdId : entry.getCreatedPermanentIds()) {
                Permanent created = gameQueryService.findPermanentById(gameData, createdId);
                if (created != null && !created.isTapped()) {
                    created.tap();
                }
            }
        }

        // Its controller chooses what each one attacks (CR 508.4).
        List<CardEffect> choices = new ArrayList<>();
        for (var createdId : entry.getCreatedPermanentIds()) {
            Permanent created = gameQueryService.findPermanentById(gameData, createdId);
            if (created != null && gameQueryService.isCreature(gameData, created)) {
                choices.add(new MakeChosenPermanentAttackingEffect(createdId));
            }
        }
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, choices);
        log.info("Game {} - {} permanent(s) made attacking by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
