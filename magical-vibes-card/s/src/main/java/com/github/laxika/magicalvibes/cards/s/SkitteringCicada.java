package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

@CardRegistration(set = "CMM", collectorNumber = "717")
@CardRegistration(set = "CMM", collectorNumber = "750")
public class SkitteringCicada extends Card {

    public SkitteringCicada() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardIsColorlessPredicate()));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new BoostSelfByCastSpellManaValueEffect(new CardIsColorlessPredicate(), Keyword.TRAMPLE));
    }
}
