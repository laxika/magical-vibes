package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "179")
public class DragonbackAssault extends Card {

    public DragonbackAssault() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(3, false, false, true, null));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new CreateTokenEffect("Dragon", 4, 4, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of()));
    }
}
