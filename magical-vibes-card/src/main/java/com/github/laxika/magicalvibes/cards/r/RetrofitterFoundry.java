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
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HA6", collectorNumber = "10")
public class RetrofitterFoundry extends Card {

    public RetrofitterFoundry() {
        CreateTokenEffect servo = new CreateTokenEffect(
                "Servo", 1, 1, null, List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT));
        CreateTokenEffect thopter = new CreateTokenEffect(
                "Thopter", 1, 1, null, List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT));
        CreateTokenEffect construct = new CreateTokenEffect(
                "Construct", 4, 4, null, List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{3}: Untap Retrofitter Foundry."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(servo),
                "{2}, {T}: Create a 1/1 colorless Servo artifact creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificePermanentCost(servoFilter(CardSubtype.SERVO), "a Servo", false),
                        thopter
                ),
                "{1}, {T}, Sacrifice a Servo: Create a 1/1 colorless Thopter artifact creature token with flying."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(servoFilter(CardSubtype.THOPTER), "a Thopter", false),
                        construct
                ),
                "{T}, Sacrifice a Thopter: Create a 4/4 colorless Construct artifact creature token."
        ));
    }

    private PermanentAllOfPredicate servoFilter(CardSubtype subtype) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(subtype)
        ));
    }
}
