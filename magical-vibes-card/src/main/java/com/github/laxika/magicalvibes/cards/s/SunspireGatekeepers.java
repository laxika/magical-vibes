package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "9")
public class SunspireGatekeepers extends Card {

    public SunspireGatekeepers() {
        // When this creature enters, if you control two or more Gates, create a 2/2 white Knight
        // creature token with vigilance. Intervening-if gate (CR 603.4): checked as the trigger
        // goes on the stack and again at resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                new CreateTokenEffect("Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of())));
    }
}
