package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "311")
@CardRegistration(set = "TPR", collectorNumber = "232")
public class Telethopter extends Card {

    public Telethopter() {
        // Tap an untapped creature you control: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
                ),
                "Tap an untapped creature you control: This creature gains flying until end of turn."
        ));
    }
}
