package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "691")
@CardRegistration(set = "TSR", collectorNumber = "395")
public class HedronArchive extends Card {

    public HedronArchive() {
        // {T}: Add {C}{C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2)),
                "{T}: Add {C}{C}."
        ));

        // {2}, {T}, Sacrifice Hedron Archive: Draw two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{2}, {T}, Sacrifice Hedron Archive: Draw two cards."
        ));
    }
}
