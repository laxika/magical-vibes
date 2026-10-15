package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TimesSourceAbilityResolvedThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "240")
public class BronzeCudgels extends Card {

    public BronzeCudgels() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new BoostEquippedCreatureUntilEndOfTurnEffect(
                        new TimesSourceAbilityResolvedThisTurn(), new Fixed(0))),
                "{2}: Until end of turn, equipped creature gets +X/+0, where X is the number of times this ability has resolved this turn."
        ));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
