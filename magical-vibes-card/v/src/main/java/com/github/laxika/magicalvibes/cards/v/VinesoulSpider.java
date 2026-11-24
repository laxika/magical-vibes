package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "18")
public class VinesoulSpider extends Card {

    public VinesoulSpider() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                SeekLibraryEffect.randomWithoutSeeking(new Fixed(1), new CardTypePredicate(CardType.LAND),
                        LibrarySearchDestination.GRAVEYARD, null));
    }
}
