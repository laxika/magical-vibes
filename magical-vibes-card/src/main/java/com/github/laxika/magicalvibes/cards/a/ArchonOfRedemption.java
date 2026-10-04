package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "3")
@CardRegistration(set = "SCD", collectorNumber = "7")
public class ArchonOfRedemption extends Card {

    public ArchonOfRedemption() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAnyOfPredicate(List.of(new PermanentIsSourcePermanentPredicate(),
                                new PermanentHasKeywordPredicate(Keyword.FLYING))),
                        new MayEffect(new GainLifeEqualToPowerEffect(),
                                "Gain life equal to that creature's power?")));
    }
}
