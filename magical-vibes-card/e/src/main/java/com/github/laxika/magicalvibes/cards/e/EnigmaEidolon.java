package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "24")
public class EnigmaEidolon extends Card {

    public EnigmaEidolon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new MillEffect(3, Subjects.targetPlayer())),
                "{U}, Sacrifice this creature: Target player mills three cards."
        ));

        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(
                        new PutCardsIntoHandEffect(Subjects.sourceCard()),
                        "Return Enigma Eidolon from your graveyard to your hand?"));
    }
}
