package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "MIR", collectorNumber = "101")
public class VaporousDjinn extends Card {

    public VaporousDjinn() {
        // "At the beginning of your upkeep, this creature phases out unless you pay {U}{U}."
        // Flying is a printed keyword loaded from Scryfall.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{U}{U}", new PhaseOutEffect(Subjects.source())));
    }
}
