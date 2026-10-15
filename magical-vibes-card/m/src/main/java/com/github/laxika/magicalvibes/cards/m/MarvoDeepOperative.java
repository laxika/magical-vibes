package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IfWonClashEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "MKC", collectorNumber = "7")
@CardRegistration(set = "MKC", collectorNumber = "315")
public class MarvoDeepOperative extends Card {

    public MarvoDeepOperative() {
        // Whenever this creature attacks, clash with the defending player.
        addEffect(EffectSlot.ON_ATTACK, new ClashEffect(null));

        // Whenever you win a clash, draw a card. Then you may cast a spell from your hand with
        // mana value 8 or less without paying its mana cost.
        addEffect(EffectSlot.ON_CONTROLLER_CLASHES, new IfWonClashEffect(SequenceEffect.of(
                new DrawCardEffect(1),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        new CardMaxManaValuePredicate(8)))));
    }
}
