package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "128")
public class DesertCerodon extends Card {

    public DesertCerodon() {
        // Cycling {R} ({R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new DrawCardEffect(1)),
                "Cycling {R} ({R}, Discard this card: Draw a card.)"));
    }
}
