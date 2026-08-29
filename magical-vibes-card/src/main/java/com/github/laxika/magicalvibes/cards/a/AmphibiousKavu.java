package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "78")
public class AmphibiousKavu extends Card {

    public AmphibiousKavu() {
        PermanentColorInPredicate blueOrBlack =
                new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.BLACK));
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfWhenCombatOpponentMatchesEffect(blueOrBlack, 3, 3));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostSelfWhenCombatOpponentMatchesEffect(blueOrBlack, 3, 3));
    }
}
