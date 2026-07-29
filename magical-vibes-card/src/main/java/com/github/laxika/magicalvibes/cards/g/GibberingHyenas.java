package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "219")
public class GibberingHyenas extends Card {

    public GibberingHyenas() {
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                "nonblack creatures"
        ));
    }
}
