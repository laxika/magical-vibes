package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "11")
@CardRegistration(set = "PIO", collectorNumber = "280")
@CardRegistration(set = "KHC", collectorNumber = "23")
public class EvangelOfHeliod extends Card {

    public EvangelOfHeliod() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSoldier(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE)));
    }
}
