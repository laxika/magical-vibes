package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "61")
public class PhantasmalForm extends Card {

    public PhantasmalForm() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(3, 3))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL,
                        new GrantColorUntilEndOfTurnEffect(
                                CardColor.BLUE, true, GrantScope.TARGETS, false))
                .addEffect(EffectSlot.SPELL,
                        new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.ILLUSION, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
