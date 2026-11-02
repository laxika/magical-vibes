package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "105")
@CardRegistration(set = "VOC", collectorNumber = "12")
@CardRegistration(set = "VOC", collectorNumber = "50")
public class EtherealInvestigator extends Card {

    public EtherealInvestigator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                CreateTokenEffect.ofClueToken(new Sum(new PlayersInGame(), new Fixed(-1))));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new NthCardDrawTriggerEffect(2, CreateTokenEffect.whiteSpirit(1)));
    }
}
