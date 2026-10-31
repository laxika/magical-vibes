package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "M19", collectorNumber = "298")
public class SarkhansDragonfire extends Card {

    public SarkhansDragonfire() {
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
        addSpellEffect(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                5, new CardColorPredicate(CardColor.RED)));
    }
}
