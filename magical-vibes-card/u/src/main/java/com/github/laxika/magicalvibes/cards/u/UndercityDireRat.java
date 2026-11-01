package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "123")
public class UndercityDireRat extends Card {

    public UndercityDireRat() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(1));
    }
}
