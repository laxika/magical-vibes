package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "598")
public class TheHowlingCommandos extends Card {

    public TheHowlingCommandos() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES,
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER)))
                ),
                "{5}: Creatures you control get +1/+1 until end of turn. Soldiers you control gain vigilance until end of turn."
        ));
    }
}
