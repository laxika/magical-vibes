package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "133")
public class ThopterFoundry extends Card {

    public ThopterFoundry() {
        // {1}, Sacrifice a nontoken artifact: Create a 1/1 blue Thopter artifact creature
        // token with flying. You gain 1 life.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                                "Sacrifice a nontoken artifact", false),
                        new CreateTokenEffect("Thopter", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                                Set.of(CardType.ARTIFACT)),
                        new GainLifeEffect(1)),
                "{1}, Sacrifice a nontoken artifact: Create a 1/1 blue Thopter artifact "
                        + "creature token with flying. You gain 1 life."));
    }
}
