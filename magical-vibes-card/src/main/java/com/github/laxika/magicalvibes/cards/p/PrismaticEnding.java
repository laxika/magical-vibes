package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentManaValueAtMostColorsSpent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandPermanentIfManaValueAtMostConvergeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "1868")
@CardRegistration(set = "SPG", collectorNumber = "40")
@CardRegistration(set = "MB2", collectorNumber = "16")
@CardRegistration(set = "SOA", collectorNumber = "7")
public class PrismaticEnding extends Card {

    public PrismaticEnding() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetNonlandPermanentIfManaValueAtMostConvergeEffect());
    }
}
