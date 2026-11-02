package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfOpponentLibraryAndBecomeCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "58")
public class ShapeshiftersMarrow extends Card {

    public ShapeshiftersMarrow() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new RevealTopCardOfOpponentLibraryAndBecomeCopyEffect());
    }
}
