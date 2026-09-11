package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "145")
public class GoroGoroDiscipleOfRyusei extends Card {

    public GoroGoroDiscipleOfRyusei() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)),
                "{R}: Creatures you control gain haste until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(new CreateTokenEffect(
                        "Dragon Spirit", 5, 5, CardColor.RED,
                        List.of(CardSubtype.DRAGON, CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()
                )),
                "{3}{R}{R}: Create a 5/5 red Dragon Spirit creature token with flying. Activate only if you control an attacking modified creature."
        ).withRequiredControlledPermanents(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsModifiedPredicate()
                )),
                1,
                "an attacking modified creature"
        ));
    }
}
