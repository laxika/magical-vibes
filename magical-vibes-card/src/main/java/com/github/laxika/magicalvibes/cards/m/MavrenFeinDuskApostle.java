package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.HasNontokenSubtypeAttackerConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "24")
public class MavrenFeinDuskApostle extends Card {

    public MavrenFeinDuskApostle() {
        // Whenever one or more nontoken Vampires you control attack,
        // create a 1/1 white Vampire creature token with lifelink.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new HasNontokenSubtypeAttackerConditionalEffect(
                        CardSubtype.VAMPIRE,
                        new CreateTokenEffect("Vampire", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of())));
    }
}
