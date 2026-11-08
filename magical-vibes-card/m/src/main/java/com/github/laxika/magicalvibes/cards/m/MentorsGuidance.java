package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "46")
public class MentorsGuidance extends Card {

    public MentorsGuidance() {
        // "Copy it if you control …" is checked as the ability resolves.
        addTrigger(Triggers.youCastThisSpell(), new ConditionalEffect(
                new ControlsPermanent(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.CLERIC,
                                CardSubtype.DRUID,
                                CardSubtype.SHAMAN,
                                CardSubtype.WARLOCK,
                                CardSubtype.WIZARD
                        ))
                ))),
                new CopyTriggeringSpellEffect()
        ));
        addSpellEffect(new ScryEffect(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}
