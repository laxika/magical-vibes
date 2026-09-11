package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfEventValueAtLeastEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "221")
public class FaridehDevilsChosen extends Card {

    public FaridehDevilsChosen() {
        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                SequenceEffect.of(
                        new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.MENACE), GrantScope.SELF),
                        new DrawCardIfEventValueAtLeastEffect(10)));
    }
}
