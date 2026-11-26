package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "233")
public class WormwoodDryad extends Card {

    public WormwoodDryad() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new GrantKeywordEffect(Keyword.FORESTWALK, Subjects.source()),
                        DealDamageEffect.to(Subjects.you(), 1)
                ),
                "{G}: This creature gains forestwalk until end of turn and deals 1 damage to you."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new GrantKeywordEffect(Keyword.SWAMPWALK, Subjects.source()),
                        DealDamageEffect.to(Subjects.you(), 1)
                ),
                "{B}: This creature gains swampwalk until end of turn and deals 1 damage to you."
        ));
    }
}
