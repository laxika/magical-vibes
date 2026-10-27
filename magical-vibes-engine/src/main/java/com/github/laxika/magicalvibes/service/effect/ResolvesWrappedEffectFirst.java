package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Marks an effect handler that only checks a condition and then resolves one wrapped effect with the same stack
 * entry, changing nothing before it. {@code EffectResolutionService} opens the Krark's Thumb choice window for the
 * wrapped effect's handler as though that handler resolved the step itself: when the wrapped handler is marked
 * {@link ChoosesCoinFlipResultsFirst} or {@link ChoosesCoinFlipResultsAtCheckpoints}, its flips may stop the step,
 * which is then resolved again through this handler (which checks its condition again and reads the flips back).
 */
public interface ResolvesWrappedEffectFirst {

    /** The effect this handler resolves for {@code effect}, the effect it handles. */
    CardEffect wrappedEffect(CardEffect effect);
}
