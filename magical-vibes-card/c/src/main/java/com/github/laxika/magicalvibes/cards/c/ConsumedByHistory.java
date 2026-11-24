package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "8")
public class ConsumedByHistory extends Card {

    public ConsumedByHistory() {
        addSpellEffect(new RegisterGlobalTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.nontokenCreatureDies(ObjectRelation.ANY),
                new PerpetuallyGrantAbilitiesEffect(Subjects.triggeringCards(),
                        List.of(Card.unearthAbility("{5}").withZones(AbilityZones.GRAVEYARD))))));
        addSpellEffect(new MassDamageEffect(3));
    }
}
