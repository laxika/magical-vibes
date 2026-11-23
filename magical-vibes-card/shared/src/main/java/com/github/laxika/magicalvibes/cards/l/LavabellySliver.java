package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "665")
@CardRegistration(set = "MH1", collectorNumber = "207")
public class LavabellySliver extends Card {

    public LavabellySliver() {
        // Sliver creatures you control have "When this creature enters, it deals 1 damage to target
        // player or planeswalker and you gain 1 life."
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                new SequenceEffect(List.of(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                        new GainLifeEffect(1)))),
                Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
