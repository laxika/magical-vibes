package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "48")
@CardRegistration(set = "LCC", collectorNumber = "80")
public class DuskLegionSergeant extends Card {

    public DuskLegionSergeant() {
        PermanentAllOfPredicate nontokenVampire = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new GrantKeywordEffect(Keyword.PERSIST, GrantScope.OWN_CREATURES, nontokenVampire)
                ),
                "Sacrifice this creature: Each nontoken Vampire creature you control gains persist until end of turn."
        ));
    }
}
