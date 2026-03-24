package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AdantoTheFirstFort;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MinimumAttackersConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "22")
public class LegionsLanding extends Card {

    public LegionsLanding() {
        // Set up back face
        AdantoTheFirstFort backFace = new AdantoTheFirstFort();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When Legion's Landing enters, create a 1/1 white Vampire creature token with lifelink.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Vampire", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of()));

        // When you attack with three or more creatures, transform Legion's Landing.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new MinimumAttackersConditionalEffect(3, new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "AdantoTheFirstFort";
    }
}
