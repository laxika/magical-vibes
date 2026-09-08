package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "85")
public class Morphling extends Card {

    public Morphling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{U}: Untap this creature."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{U}: This creature gains flying until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.SELF)),
                "{U}: This creature gains shroud until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(1, -1)),
                "{1}: This creature gets +1/-1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(-1, 1)),
                "{1}: This creature gets -1/+1 until end of turn."
        ));
    }
}
