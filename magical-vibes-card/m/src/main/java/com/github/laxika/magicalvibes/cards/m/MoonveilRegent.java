package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "149")
@CardRegistration(set = "DBL", collectorNumber = "149")
public class MoonveilRegent extends Card {

    public MoonveilRegent() {
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new SpellCastTriggerEffect(null,
                        List.of(new DiscardOwnHandThenDrawEffect(new TriggeringSpellColorCount()))),
                "Discard your hand and draw a card for each of that spell's colors?"));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DealDamageToAnyTargetEffect(new ColorsAmongControlledPermanents()));
    }
}
