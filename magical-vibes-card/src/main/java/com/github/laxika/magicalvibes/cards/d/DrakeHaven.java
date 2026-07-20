package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "51")
public class DrakeHaven extends Card {

    public DrakeHaven() {
        // Whenever you cycle or discard a card, you may pay {1}. If you do, create a 2/2 blue Drake with
        // flying. Cycling is a discard (CR 702.29e), so the single "controller discards" trigger covers both.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new MayPayManaEffect("{1}",
                        new CreateTokenEffect("Drake", 2, 2,
                                CardColor.BLUE,
                                List.of(CardSubtype.DRAKE),
                                Set.of(Keyword.FLYING),
                                Set.of()),
                        "Pay {1} to create a 2/2 blue Drake creature token with flying?"));
    }
}
