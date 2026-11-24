package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "132")
public class DualStrike extends Card {

    public DualStrike() {
        addSpellEffect(
                RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                        CardPredicateUtils.instantOrSorceryWithManaValueAtMost(4),
                        List.of(new CopyTriggeringSpellEffect())));
        addCastingOption(new ForetellCast("{R}"));
    }
}
