package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "ALL", collectorNumber = "73a")
@CardRegistration(set = "ALL", collectorNumber = "73b")
public class GorillaWarCry extends Card {

    public GorillaWarCry() {
        // Cast this spell only during combat before blockers are declared.
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT_BEFORE_BLOCKERS);

        // All creatures gain menace until end of turn.
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_CREATURES));

        // Draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
