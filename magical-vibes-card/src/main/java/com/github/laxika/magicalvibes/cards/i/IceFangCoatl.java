package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "171")
@CardRegistration(set = "SLD", collectorNumber = "1225")
@CardRegistration(set = "MH1", collectorNumber = "203")
public class IceFangCoatl extends Card {

    public IceFangCoatl() {
        // When this creature enters, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));

        // This creature has deathtouch as long as you control at least three other snow permanents.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsOtherPermanentCount(3, new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)
        ));
    }
}
