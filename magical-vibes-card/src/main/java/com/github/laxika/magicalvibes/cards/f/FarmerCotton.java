package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "55")
@CardRegistration(set = "LTC", collectorNumber = "138")
public class FarmerCotton extends Card {

    public FarmerCotton() {
        // When this creature enters, create X 1/1 white Halfling creature tokens and X Food tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new CreateTokenEffect(new XValue(), "Halfling", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.HALFLING), Set.of(), Set.of()),
                CreateTokenEffect.ofArtifactToken(new XValue(), "Food", List.of(CardSubtype.FOOD),
                        List.of(new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this token: You gain 3 life.")))
        ));
    }
}
