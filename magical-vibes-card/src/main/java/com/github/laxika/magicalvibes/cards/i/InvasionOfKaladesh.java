package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AetherwingGoldenScaleFlagship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "234")
public class InvasionOfKaladesh extends Card {

    public InvasionOfKaladesh() {
        setBackFaceCard(new AetherwingGoldenScaleFlagship());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Thopter", 1, 1, null,
                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));
    }

    @Override
    public String getBackFaceClassName() {
        return "AetherwingGoldenScaleFlagship";
    }
}
