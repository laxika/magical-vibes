package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "18")
public class SunsBounty extends Card {

    public SunsBounty() {
        addSpellEffect(new GainLifeEffect(4));

        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayPayManaEffect("{1}{W}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {1}{W} to return Sun's Bounty from your graveyard to your hand?",
                        new ExileSourceCardFromGraveyardEffect()));
    }
}
