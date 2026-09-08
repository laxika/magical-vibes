package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "127")
public class VitoThornOfTheDuskRose extends Card {

    public VitoThornOfTheDuskRose() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{B}",
                List.of(
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)
                ),
                "{3}{B}{B}: Creatures you control gain lifelink until end of turn."
        ));
    }
}
