package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithPlotEffect;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Marks a still-resolving hand-cast multicolored instant or sorcery for plot exile. */
@Component
public class ExileTriggeringSpellWithPlotEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellWithPlotEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) return;

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> candidate.getSourceZone() == Zone.HAND)
                .filter(candidate -> candidate.getCard().hasType(CardType.INSTANT)
                        || candidate.getCard().hasType(CardType.SORCERY))
                .findFirst()
                .orElse(null);
        if (spell == null) return;

        if (spell.getEffectsToResolve().stream().anyMatch(ExileSpellEffect.class::isInstance)) return;

        gameData.spellsWithPlotOnResolution.add(triggeringCardId);
    }
}
