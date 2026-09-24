package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentManaValueAtMostColorsSpent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "1868")
public class PrismaticEnding extends Card {

    public PrismaticEnding() {
        // The mana-value clause is a resolution-time condition, not a targeting restriction,
        // because the colors spent are known only after targets are chosen.
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.SPELL,
                new ConditionalEffect(
                        new TargetPermanentManaValueAtMostColorsSpent(),
                        new ExileTargetPermanentEffect()));
    }
}
