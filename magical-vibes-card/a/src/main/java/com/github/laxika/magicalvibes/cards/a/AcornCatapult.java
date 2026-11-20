package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMD", collectorNumber = "241")
public class AcornCatapult extends Card {

    public AcornCatapult() {
        addActivatedAbility(new ActivatedAbility(true, "{1}", List.of(
                new DealDamageToAnyTargetEffect(1),
                new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())
                        .forController(Subjects.allOf(Subjects.controllerOf(Subjects.target()),
                                Subjects.targetPlayerDeclaredElsewhere()))),
                "{1}, {T}: This artifact deals 1 damage to any target. That permanent's controller or that player creates a 1/1 green Squirrel creature token."));
    }
}
