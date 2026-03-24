package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "25")
public class PaladinOfTheBloodstained extends Card {

    public PaladinOfTheBloodstained() {
        // When Paladin of the Bloodstained enters, create a 1/1 white Vampire creature token with lifelink.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Vampire", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of()));
    }
}
