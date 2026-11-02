package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "35")
@CardRegistration(set = "NCC", collectorNumber = "136")
@CardRegistration(set = "MKC", collectorNumber = "127")
public class DoggedDetective extends Card {

    public DoggedDetective() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(2));
        addTrigger(new TriggerCondition(Triggers.opponentDrawsSecondCardEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Dogged Detective from your graveyard to your hand?"));
    }
}
