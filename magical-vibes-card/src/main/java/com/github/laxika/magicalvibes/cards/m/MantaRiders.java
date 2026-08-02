package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "74")
public class MantaRiders extends Card {

    public MantaRiders() {
        // {U}: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{U}: Manta Riders gains flying until end of turn."
        ));
    }
}
