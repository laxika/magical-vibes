package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;

@CardRegistration(set = "MKC", collectorNumber = "105")
public class EtherealInvestigator extends Card {

    public EtherealInvestigator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CreateTokenEffect.ofClueToken(new Sum(new PlayersInGame(), new Fixed(-1))));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new NthCardDrawTriggerEffect(2, CreateTokenEffect.whiteSpirit(1)));
    }
}
