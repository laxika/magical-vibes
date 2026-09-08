package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "165")
public class OviyaPashiriSageLifecrafter extends Card {

    public OviyaPashiriSageLifecrafter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new CreateTokenEffect(
                        1, "Servo", 1, 1, null,
                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))),
                "{2}{G}, {T}: Create a 1/1 colorless Servo artifact creature token."
        ));

        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{G}",
                List.of(new CreateTokenEffect(
                        "Construct", creaturesYouControl, creaturesYouControl, null,
                        List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT))),
                "{4}{G}, {T}: Create an X/X colorless Construct artifact creature token, where X is the number of creatures you control."
        ));
    }
}
