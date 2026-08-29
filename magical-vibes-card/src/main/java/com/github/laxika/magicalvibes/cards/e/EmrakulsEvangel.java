package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "156")
public class EmrakulsEvangel extends Card {

    public EmrakulsEvangel() {
        var nonEldraziCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ELDRAZI))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new SacrificeXPermanentsCost(nonEldraziCreature),
                        new CreateTokenEffect(
                                new Sum(new Fixed(1), new XValue()),
                                "Eldrazi Horror",
                                3,
                                2,
                                null,
                                List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR),
                                Set.of(),
                                Set.of())
                ),
                "{T}, Sacrifice this creature and any number of other non-Eldrazi creatures: Create a 3/2 colorless Eldrazi Horror creature token for each creature sacrificed this way."
        ).withXValue());
    }
}
