package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "228")
public class ChromaticOrrery extends Card {

    public ChromaticOrrery() {
        addEffect(EffectSlot.STATIC, new SpendManaAsAnyColorEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 5)),
                "{T}: Add {C}{C}{C}{C}{C}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new DrawCardEffect(new ColorsAmongControlledPermanents())),
                "{5}, {T}: Draw a card for each color among permanents you control."
        ));
    }
}
