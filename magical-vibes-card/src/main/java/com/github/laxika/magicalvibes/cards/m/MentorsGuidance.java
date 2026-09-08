package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "46")
public class MentorsGuidance extends Card {

    public MentorsGuidance() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfConditionEffect(
                new ControlsPermanent(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.CLERIC,
                                CardSubtype.DRUID,
                                CardSubtype.SHAMAN,
                                CardSubtype.WARLOCK,
                                CardSubtype.WIZARD
                        ))
                )))
        ));
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
