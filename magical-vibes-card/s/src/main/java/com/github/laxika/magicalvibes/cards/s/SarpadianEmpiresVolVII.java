package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOfChosenColorAndSubtypeEffect;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "TSP", collectorNumber = "263")
public class SarpadianEmpiresVolVII extends Card {

    public SarpadianEmpiresVolVII() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new CreateTokenOfChosenColorAndSubtypeEffect(1, 1, Map.of(
                        CardColor.WHITE, CardSubtype.CITIZEN,
                        CardColor.BLUE, CardSubtype.CAMARID,
                        CardColor.BLACK, CardSubtype.THRULL,
                        CardColor.RED, CardSubtype.GOBLIN,
                        CardColor.GREEN, CardSubtype.SAPROLING
                ))),
                "{3}, {T}: Create a 1/1 creature token of the chosen color and type."
        ));
    }
}
