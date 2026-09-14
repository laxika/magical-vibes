package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLC", collectorNumber = "18")
@CardRegistration(set = "SLC", collectorNumber = "45")
public class RetrofitterFoundry extends Card {

    public RetrofitterFoundry() {
        // {3}: Untap this artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{3}: Untap Retrofitter Foundry."
        ));

        // {2}, {T}: Create a 1/1 colorless Servo artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new CreateTokenEffect(
                        1, "Servo", 1, 1, null,
                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))),
                "{2}, {T}: Create a 1/1 colorless Servo artifact creature token."
        ));

        // {1}, {T}, Sacrifice a Servo: Create a 1/1 colorless Thopter artifact creature token
        // with flying.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SERVO),
                                "Sacrifice a Servo", false),
                        new CreateTokenEffect(
                                1, "Thopter", 1, 1, null,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))),
                "{1}, {T}, Sacrifice a Servo: Create a 1/1 colorless Thopter artifact creature token with flying."
        ));

        // {T}, Sacrifice a Thopter: Create a 4/4 colorless Construct artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.THOPTER),
                                "Sacrifice a Thopter", false),
                        new CreateTokenEffect(
                                1, "Construct", 4, 4, null,
                                List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT))),
                "{T}, Sacrifice a Thopter: Create a 4/4 colorless Construct artifact creature token."
        ));
    }
}
