package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellExile;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Marks the still-resolving instant or sorcery spell that caused this trigger so that its disposition in
 * {@code StackResolutionService} exiles it instead of putting it into the graveyard. Nothing is marked
 * when the spell is gone from the stack (countered), is a copy, or already moves itself elsewhere as part
 * of its own effects (an {@link ExileSpellEffect}, {@link ShuffleIntoLibraryEffect} or
 * {@link PutSelfOnBottomOfOwnersLibraryEffect}), since it would then never be put into a graveyard.
 */
@Component
public class ExileTriggeringSpellEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTriggeringSpellEffect exileEffect = (ExileTriggeringSpellEffect) effect;
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) return;

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> candidate.getCard() != null)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> !candidate.isCopy())
                .filter(candidate -> candidate.getCard().hasType(CardType.INSTANT)
                        || candidate.getCard().hasType(CardType.SORCERY))
                .findFirst()
                .orElse(null);
        if (spell == null) return;
        if (exileEffect.yourGraveyardOnly() && !entry.getControllerId().equals(spell.getOwnerId())) return;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (exileEffect.exile() == TriggeringSpellExile.TRACKED_BY_SOURCE) {
            if (sourcePermanentId == null) return;
            if (spell.getExileWithSourcePermanentId() != null
                    && !sourcePermanentId.equals(spell.getExileWithSourcePermanentId())
                    && spell.getEffectsToResolve().stream().anyMatch(ExileSpellEffect.class::isInstance)) {
                // Another source already marked the spell: both replacement effects apply, and the
                // spell's controller picks which one is used as the spell resolves (CR 616.1).
                if (!spell.getCompetingExileSourcePermanentIds().contains(sourcePermanentId)) {
                    spell.getCompetingExileSourcePermanentIds().add(sourcePermanentId);
                }
                return;
            }
        }
        if (spell.getEffectsToResolve().stream().anyMatch(candidateEffect ->
                candidateEffect instanceof ExileSpellEffect
                        || candidateEffect instanceof ShuffleIntoLibraryEffect
                        || candidateEffect instanceof PutSelfOnBottomOfOwnersLibraryEffect)) {
            return;
        }

        switch (exileEffect.exile()) {
            case TRACKED_BY_SOURCE -> {
                spell.getEffectsToResolve().add(new ExileSpellEffect());
                spell.setExileWithSourcePermanentId(sourcePermanentId);
            }
            case DREAM_COUNTER -> gameData.spellsWithDreamCounterOnResolution.add(triggeringCardId);
            case PLOTTED -> gameData.spellsWithPlotOnResolution.add(triggeringCardId);
            case SUSPENDED -> spell.getEffectsToResolve().add(new ExileSpellEffect(exileEffect.suspendTimeCounters()));
            case RETURN_TO_HAND_AT_NEXT_END_STEP -> {
                spell.setExileAndReturnToHandAtNextEndStep(true);
                spell.setExileAndReturnSourceCard(entry.getCard());
            }
        }
    }
}
