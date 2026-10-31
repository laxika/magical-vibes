package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Back face of {@link GutterSkulker}. */
public class GutterShortcut extends Card {

    public GutterShortcut() {
        target(TargetFilters.creature())
                .addStaticEffect(new CantBeBlockedIfAttackingAloneEffect());
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
