package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "64")
public class KavuGlider extends Card {

    public KavuGlider() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(BoostEffect.self(0, 1)),
                "{W}: This creature gets +0/+1 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, Subjects.source())),
                "{U}: This creature gains flying until end of turn."));
    }
}
