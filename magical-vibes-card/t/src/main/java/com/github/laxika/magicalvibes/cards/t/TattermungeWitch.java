package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "220")
@CardRegistration(set = "MM3", collectorNumber = "212")
public class TattermungeWitch extends Card {

    public TattermungeWitch() {
        addActivatedAbility(new ActivatedAbility(false, "{R}{G}", List.of(
                BoostEffect.allCreatures(1, 0, new PermanentIsBlockedPredicate()),
                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.allCreatures(), new PermanentIsBlockedPredicate())),
                "{R}{G}: Each blocked creature gets +1/+0 and gains trample until end of turn."));
    }
}
