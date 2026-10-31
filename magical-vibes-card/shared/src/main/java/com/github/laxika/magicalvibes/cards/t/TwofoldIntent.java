package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Twofold Intent — the prepare spell of Quill-Blade Laureate // Twofold Intent (SOS 27).
 * <p>
 * Sorcery: Target creature gets +1/+0 and gains double strike until end of turn.
 */
public class TwofoldIntent extends Card {

    public TwofoldIntent() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
    }
}
