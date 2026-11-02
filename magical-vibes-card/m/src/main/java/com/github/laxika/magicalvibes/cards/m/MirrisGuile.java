package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "236")
@CardRegistration(set = "MB2", collectorNumber = "209")
public class MirrisGuile extends Card {

    public MirrisGuile() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new ReorderTopCardsOfLibraryEffect(3),
                "Look at the top three cards of your library and put them back in any order?"
        ));
    }
}
