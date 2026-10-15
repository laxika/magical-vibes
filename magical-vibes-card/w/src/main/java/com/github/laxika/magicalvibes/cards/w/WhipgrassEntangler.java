package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "26")
public class WhipgrassEntangler extends Card {

    public WhipgrassEntangler() {
        PermanentCount clericsOnBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC), CountScope.ANY_PLAYER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantStaticEffectToTargetUntilEndOfTurnEffect(
                        new CantAttackOrBlockUnlessPaysEffect(clericsOnBattlefield))),
                "{1}{W}: Until end of turn, target creature gains \"This creature can't attack or block "
                        + "unless its controller pays {1} for each Cleric on the battlefield.\"",
                TargetFilters.creature()));
    }
}
