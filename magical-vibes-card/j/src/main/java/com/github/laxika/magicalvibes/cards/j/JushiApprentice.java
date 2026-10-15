package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TomoyaTheRevealer;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "70")
public class JushiApprentice extends Card {

    public JushiApprentice() {
        setBackFaceCard(new TomoyaTheRevealer());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        new DrawCardEffect(1),
                        new ConditionalEffect(new CardsInHandAtLeast(9), new TransformToBackFaceEffect())
                ),
                "{2}{U}, {T}: Draw a card. If you have nine or more cards in hand, flip this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TomoyaTheRevealer";
    }
}
