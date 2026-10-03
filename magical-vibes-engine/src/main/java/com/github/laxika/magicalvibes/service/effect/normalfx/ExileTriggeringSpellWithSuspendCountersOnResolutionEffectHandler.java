package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSuspendCountersOnResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Marks a hand-cast instant or sorcery for suspend exile after successful resolution. */
@Component
public class ExileTriggeringSpellWithSuspendCountersOnResolutionEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellWithSuspendCountersOnResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTriggeringSpellWithSuspendCountersOnResolutionEffect suspendEffect =
                (ExileTriggeringSpellWithSuspendCountersOnResolutionEffect) effect;
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) return;

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> candidate.getCard() != null)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> !candidate.isCopy())
                .filter(candidate -> candidate.getSourceZone() == Zone.HAND)
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

        spell.getEffectsToResolve().add(new ExileSpellEffect(suspendEffect.timeCounters()));
    }
}
