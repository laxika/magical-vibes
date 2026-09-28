package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Marks the triggering instant or sorcery for source-tracked exile on resolution. */
@Component
public class ExileTriggeringSpellWithSourceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (triggeringCardId == null || sourcePermanentId == null) return;

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> candidate.getCard() != null)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> !candidate.isCopy())
                .filter(candidate -> candidate.getCard().hasType(CardType.INSTANT)
                        || candidate.getCard().hasType(CardType.SORCERY))
                .findFirst()
                .orElse(null);
        if (spell == null || spell.getEffectsToResolve().stream().anyMatch(candidateEffect ->
                candidateEffect instanceof ExileSpellEffect
                        || candidateEffect instanceof ShuffleIntoLibraryEffect
                        || candidateEffect instanceof PutSelfOnBottomOfOwnersLibraryEffect)) {
            return;
        }

        spell.getEffectsToResolve().add(new ExileSpellEffect());
        spell.setExileWithSourcePermanentId(sourcePermanentId);
    }
}
