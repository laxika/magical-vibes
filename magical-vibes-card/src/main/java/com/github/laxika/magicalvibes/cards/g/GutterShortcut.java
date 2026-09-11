package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Back face of {@link GutterSkulker}. */
public class GutterShortcut extends Card {

    public GutterShortcut() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                        new CantBeBlockedIfAttackingAloneEffect(), GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
