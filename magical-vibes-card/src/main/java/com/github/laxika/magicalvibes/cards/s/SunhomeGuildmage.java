package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "200")
public class SunhomeGuildmage extends Card {

    public SunhomeGuildmage() {
        // {1}{R}{W}: Creatures you control get +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{W}",
                List.of(new BoostAllCreaturesEffect(1, 0, new PermanentControlledBySourceControllerPredicate())),
                "{1}{R}{W}: Creatures you control get +1/+0 until end of turn."
        ));

        // {2}{R}{W}: Create a 1/1 red and white Soldier creature token with haste.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{W}",
                List.of(new CreateTokenEffect(CardType.CREATURE, 1, "Soldier", 1, 1,
                        CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                        List.of(CardSubtype.SOLDIER), Set.of(Keyword.HASTE), Set.of(),
                        false, false, Map.of(), List.of(), false, false, false, 0, Set.of())),
                "{2}{R}{W}: Create a 1/1 red and white Soldier creature token with haste."
        ));
    }
}
