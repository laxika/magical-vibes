package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "40")
@CardRegistration(set = "DMR", collectorNumber = "52")
public class GlintwingInvoker extends Card {

    public GlintwingInvoker() {
        addActivatedAbility(new ActivatedAbility(false, "{7}{U}",
                List.of(BoostEffect.self(3, 3), new GrantKeywordEffect(Keyword.FLYING, Subjects.source())),
                "{7}{U}: This creature gets +3/+3 and gains flying until end of turn."));
    }
}
