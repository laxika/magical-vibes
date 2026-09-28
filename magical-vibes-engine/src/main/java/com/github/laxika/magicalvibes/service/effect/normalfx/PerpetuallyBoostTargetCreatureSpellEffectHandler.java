package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureSpellEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies a perpetual power/toughness change to a targeted creature spell. */
@Component
public class PerpetuallyBoostTargetCreatureSpellEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostTargetCreatureSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostTargetCreatureSpellEffect) effect;
        List<UUID> boundTargets = entry.targetsForBoundEffectGroup(effect);
        UUID targetId = boundTargets == null
                ? entry.getTargetId()
                : boundTargets.stream().findFirst().orElse(null);
        if (targetId == null) {
            return;
        }

        StackEntry targetSpell = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetId))
                .findFirst()
                .orElse(null);
        if (targetSpell == null || targetSpell.getCard() == null
                || !targetSpell.getCard().hasType(CardType.CREATURE)) {
            return;
        }

        Card copy = targetSpell.getCard().createRuntimeCopy();
        if (copy.getPower() != null) {
            copy.setPower(copy.getPower() + boost.powerBoost());
        }
        if (copy.getToughness() != null) {
            copy.setToughness(copy.getToughness() + boost.toughnessBoost());
        }
        targetSpell.setCastCard(copy);
        targetSpell.setPhysicalCard(copy);
    }
}
