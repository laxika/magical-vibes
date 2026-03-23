package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "106")
public class FathomFleetCaptain extends Card {

    public FathomFleetCaptain() {
        addEffect(EffectSlot.ON_ATTACK,
                new ControlsAnotherSubtypeConditionalEffect(CardSubtype.PIRATE, true,
                        new MayPayManaEffect("{2}",
                                new CreateTokenEffect("Pirate", 2, 2,
                                        CardColor.BLACK,
                                        List.of(CardSubtype.PIRATE),
                                        Set.of(Keyword.MENACE),
                                        Set.of()),
                                "Pay {2} to create a 2/2 black Pirate creature token with menace?"
                        )
                )
        );
    }
}
