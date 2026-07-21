package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "69")
public class LurchingRotbeast extends Card {

    public LurchingRotbeast() {
        // Cycling {B} ({B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new DrawCardEffect(1)),
                "Cycling {B} ({B}, Discard this card: Draw a card.)"));
    }
}
