package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "174")
public class GarrukPrimalHunter extends Card {

    public GarrukPrimalHunter() {
        // +1: Create a 3/3 green Beast creature token.
        addActivatedAbility(new ActivatedAbility(
                1,
                List.of(new CreateTokenEffect("Beast", 3, 3,
                        CardColor.GREEN, List.of(CardSubtype.BEAST),
                        Set.of(), Set.of())),
                "+1: Create a 3/3 green Beast creature token."
        ));

        // −3: Draw cards equal to the greatest power among creatures you control.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DrawCardEffect(new GreatestPowerAmongControlled())),
                "−3: Draw cards equal to the greatest power among creatures you control."
        ));

        // −6: Create a 6/6 green Wurm creature token for each land you control.
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateTokenEffect(
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER),
                        "Wurm", 6, 6,
                        CardColor.GREEN, List.of(CardSubtype.WURM),
                        Set.of(), Set.of())),
                "−6: Create a 6/6 green Wurm creature token for each land you control."
        ));
    }
}
