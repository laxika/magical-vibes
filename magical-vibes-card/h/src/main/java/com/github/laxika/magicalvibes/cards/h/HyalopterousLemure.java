package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "133")
@CardRegistration(set = "ME1", collectorNumber = "72")
@CardRegistration(set = "DMR", collectorNumber = "89")
public class HyalopterousLemure extends Card {

    public HyalopterousLemure() {
        // {0}: This creature gets -1/-0 and gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(BoostEffect.self(-1, 0), new GrantKeywordEffect(Keyword.FLYING, Subjects.source())),
                "{0}: This creature gets -1/-0 and gains flying until end of turn."));
    }
}
