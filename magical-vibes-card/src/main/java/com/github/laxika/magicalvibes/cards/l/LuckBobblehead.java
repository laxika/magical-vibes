package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfEventValueEqualsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Arrays;
import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "135")
@CardRegistration(set = "PIP", collectorNumber = "663")
@CardRegistration(set = "PIP", collectorNumber = "1063")
public class LuckBobblehead extends Card {

    public LuckBobblehead() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {1}, {T}: Roll X six-sided dice, where X is the number of Bobbleheads you control.
        // Create a tapped Treasure token for each even result. If you rolled 6 exactly seven
        // times, you win the game.
        RollD6Effect roll = new RollD6Effect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.BOBBLEHEAD), CountScope.CONTROLLER),
                Arrays.asList(
                        null,
                        CreateTokenEffect.ofTappedTreasureToken(1),
                        null,
                        CreateTokenEffect.ofTappedTreasureToken(1),
                        null,
                        CreateTokenEffect.ofTappedTreasureToken(1)))
                .withTrackedResult(6);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(roll, new WinGameIfEventValueEqualsEffect(7)),
                "{1}, {T}: Roll X six-sided dice, where X is the number of Bobbleheads you control. "
                        + "Create a tapped Treasure token for each even result. If you rolled 6 exactly "
                        + "seven times, you win the game."
        ));
    }
}
