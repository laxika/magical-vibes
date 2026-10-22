package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.model.replacement.LifeGainEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOptionPreview;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;

import java.util.List;

/**
 * Picks which replacement effect to apply next (CR 616.1) for a player that is the event's
 * chooser: the option that can still lead to the least damage, or for a land tapped for mana, to
 * the most mana, life or tokens. Ties, and prompts without previews, keep the first option.
 */
public final class ReplacementOrderChoice {

    private ReplacementOrderChoice() {
    }

    public static String choose(ChoiceContext.ReplacementEffectOrder order, List<String> options) {
        boolean moreIsBetter = order.event() instanceof LandManaEvent || order.event() instanceof LifeGainEvent
                || order.event() instanceof TokenCreationEvent;
        String best = options.getFirst();
        Integer bestScore = null;
        for (ReplacementOptionPreview preview : order.previews()) {
            if (!options.contains(preview.label())) {
                continue;
            }
            int score = moreIsBetter ? -preview.most() : preview.fewest();
            if (bestScore == null || score < bestScore) {
                bestScore = score;
                best = preview.label();
            }
        }
        return best;
    }
}
