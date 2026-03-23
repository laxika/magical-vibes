package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "XLN", collectorNumber = "15")
public class ImperialLancer extends Card {

    public ImperialLancer() {
        addEffect(EffectSlot.STATIC, new ControlsSubtypeConditionalEffect(
                CardSubtype.DINOSAUR,
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)
        ));
    }
}
