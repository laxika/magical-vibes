package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "13")
public class FuriousForebear extends Card {

    public FuriousForebear() {
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayPayManaEffect("{1}{W}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {1}{W} to return Furious Forebear from your graveyard to your hand?"));
    }
}
