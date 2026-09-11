package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "65")
public class JacobHaukenInspector extends Card {

    public JacobHaukenInspector() {
        setBackFaceCard(new HaukensInsight());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DrawCardEffect(1),
                        new ExileCardFromHandFaceDownWithSourceEffect(false),
                        new MayPayManaEffect("{4}{U}{U}", new TransformSelfEffect(),
                                "Pay {4}{U}{U} to transform Jacob Hauken?")),
                "{T}: Draw a card, then exile a card from your hand face down. You may pay "
                        + "{4}{U}{U}. If you do, transform Jacob Hauken.")
        );
    }

    @Override
    public String getBackFaceClassName() {
        return "HaukensInsight";
    }
}
