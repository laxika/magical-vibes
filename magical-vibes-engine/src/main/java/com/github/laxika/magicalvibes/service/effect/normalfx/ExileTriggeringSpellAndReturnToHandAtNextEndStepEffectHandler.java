package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellAndReturnToHandAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Marks a successfully resolving spell for Feather's exile-and-return replacement effect. */
@Component
public class ExileTriggeringSpellAndReturnToHandAtNextEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellAndReturnToHandAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> !candidate.isCopy())
                .filter(candidate -> candidate.getCard().hasType(CardType.INSTANT)
                        || candidate.getCard().hasType(CardType.SORCERY))
                .findFirst()
                .orElse(null);
        if (spell == null || !entry.getControllerId().equals(spell.getOwnerId())) {
            return;
        }

        if (spell.getEffectsToResolve().stream().anyMatch(candidateEffect -> candidateEffect instanceof ExileSpellEffect
                || candidateEffect instanceof ShuffleIntoLibraryEffect
                || candidateEffect instanceof PutSelfOnBottomOfOwnersLibraryEffect)) {
            return;
        }

        spell.setExileAndReturnToHandAtNextEndStep(true);
    }
}
