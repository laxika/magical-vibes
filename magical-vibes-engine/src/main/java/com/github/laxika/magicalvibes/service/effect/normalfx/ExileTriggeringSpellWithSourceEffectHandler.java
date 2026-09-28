package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSourceEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Marks the triggering hand-cast instant or sorcery for source-tracked exile. */
@Component
public class ExileTriggeringSpellWithSourceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (sourcePermanentId == null || triggeringCardId == null) return;

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> !candidate.isCopy())
                .filter(candidate -> candidate.getSourceZone() == Zone.HAND)
                .filter(candidate -> candidate.getCard().hasType(CardType.INSTANT)
                        || candidate.getCard().hasType(CardType.SORCERY))
                .findFirst()
                .orElse(null);
        if (spell == null) return;

        ExileSpellEffect existing = spell.getEffectsToResolve().stream()
                .filter(ExileSpellEffect.class::isInstance)
                .map(ExileSpellEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (existing != null) {
            int index = spell.getEffectsToResolve().indexOf(existing);
            spell.replaceEffectToResolve(index, new ExileSpellEffect(
                    existing.suspendTimeCounters(), existing.screamCounterCount(), sourcePermanentId));
        } else {
            spell.insertEffectsToResolve(0, List.of(new ExileSpellEffect(0, 0, sourcePermanentId)));
        }
    }
}
