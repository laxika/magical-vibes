package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class AmphibianAccident extends Card {

    public AmphibianAccident() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new GrantColorUntilEndOfTurnEffect(CardColor.BLUE))
                .addEffect(EffectSlot.SPELL,
                        new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.FROG))
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(1, 1));
    }
}
