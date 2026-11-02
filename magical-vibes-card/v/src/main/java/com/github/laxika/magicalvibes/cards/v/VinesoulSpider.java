package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "18")
public class VinesoulSpider extends Card {

    public VinesoulSpider() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new SeekFromLibraryToGraveyardEffect(new CardTypePredicate(CardType.LAND)));
    }
}
