package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "163")
public class MerchantsDockhand extends Card {

    public MerchantsDockhand() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(
                        new TapMultiplePermanentsCost(new XValue(), new PermanentIsArtifactPredicate(), true),
                        LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new XValue())
                ),
                "{3}{U}, {T}, Tap X untapped artifacts you control: Look at the top X cards of your library. "
                        + "Put one of them into your hand and the rest on the bottom of your library in any order."
        ));
    }
}
