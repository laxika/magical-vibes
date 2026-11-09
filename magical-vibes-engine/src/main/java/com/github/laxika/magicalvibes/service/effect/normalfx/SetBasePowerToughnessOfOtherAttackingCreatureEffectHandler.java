package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessOfOtherAttackingCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "That creature" is the one other creature declared as an attacker with the source, read from the stack entry's
 * attacking creatures as the ability resolves; it is affected wherever it is on the battlefield.
 */
@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessOfOtherAttackingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final SetBasePowerToughnessEffectHandler setBasePowerToughnessEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessOfOtherAttackingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetBasePowerToughnessOfOtherAttackingCreatureEffect) effect;
        List<UUID> others = entry.getAttackingPermanentSnapshots().stream()
                .map(Permanent::getId)
                .filter(id -> !id.equals(entry.getSourcePermanentId()))
                .toList();
        if (others.size() != 1) {
            return;
        }
        Permanent other = gameQueryService.findPermanentById(gameData, others.getFirst());
        if (other == null) {
            return;
        }
        setBasePowerToughnessEffectHandler.resolveOn(gameData, entry,
                new SetBasePowerToughnessEffect(e.power(), e.toughness()), other);
    }
}
