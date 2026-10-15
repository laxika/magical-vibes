package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "83")
public class Dreamwinder extends Card {

    public Dreamwinder() {
        // This creature can't attack unless defending player controls an Island.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));

        // {U}, Sacrifice an Island: Target land becomes an Island until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                                "an Island"
                        ),
                        new GrantBasicLandTypeToTargetEffect(EffectDuration.UNTIL_END_OF_TURN, CardSubtype.ISLAND, true)
                ),
                "{U}, Sacrifice an Island: Target land becomes an Island until end of turn.",
                TargetFilters.land()
        ));
    }
}
