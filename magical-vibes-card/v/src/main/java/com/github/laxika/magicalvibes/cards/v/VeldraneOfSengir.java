package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "60")
public class VeldraneOfSengir extends Card {

    public VeldraneOfSengir() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}{B}",
                List.of(BoostEffect.self(-3, 0), new GrantKeywordEffect(Keyword.FORESTWALK, Subjects.source())),
                "{1}{B}{B}: Veldrane gets -3/-0 and gains forestwalk until end of turn."));
    }
}
