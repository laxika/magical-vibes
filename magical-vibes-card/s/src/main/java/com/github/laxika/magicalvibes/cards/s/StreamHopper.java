package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "113")
public class StreamHopper extends Card {

    public StreamHopper() {
        // {U/R}: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{U/R}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, Subjects.source())),
                "{U/R}: This creature gains flying until end of turn."));
    }
}
