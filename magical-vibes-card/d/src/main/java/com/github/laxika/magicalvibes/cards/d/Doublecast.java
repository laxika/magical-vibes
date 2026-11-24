package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "137")
@CardRegistration(set = "XANA", collectorNumber = "37")
public class Doublecast extends Card {

    public Doublecast() {
        addSpellEffect(
                RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                        CardPredicateUtils.instantOrSorcery(), List.of(new CopyTriggeringSpellEffect())));
    }
}
