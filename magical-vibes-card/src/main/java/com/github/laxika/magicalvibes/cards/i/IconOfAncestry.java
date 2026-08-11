package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "229")
public class IconOfAncestry extends Card {

    public IconOfAncestry() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(1, 1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        3, new CardHasSourceChosenSubtypePredicate())),
                "{3}, {T}: Look at the top three cards of your library. You may reveal a creature card of the chosen type from among them and put it into your hand. Put the rest on the bottom of your library in a random order."
        ));
    }
}
