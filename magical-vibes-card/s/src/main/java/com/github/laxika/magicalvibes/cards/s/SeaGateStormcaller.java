package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "77")
public class SeaGateStormcaller extends Card {

    public SeaGateStormcaller() {
        addStaticEffect(new KickerEffect("{4}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new NotKicked(),
                        RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                                CardPredicateUtils.instantOrSorceryWithManaValueAtMost(2),
                                List.of(new CopyTriggeringSpellEffect()))),
                new ConditionalEffect(new Kicked(),
                        RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                                CardPredicateUtils.instantOrSorceryWithManaValueAtMost(2),
                                List.of(new CopyTriggeringSpellEffect().withCopies(2)))));
    }
}
