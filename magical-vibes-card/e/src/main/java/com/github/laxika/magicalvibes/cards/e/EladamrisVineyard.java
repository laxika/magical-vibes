package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "223")
@CardRegistration(set = "SLD", collectorNumber = "1692")
public class EladamrisVineyard extends Card {

    public EladamrisVineyard() {
        // At the beginning of each player's first main phase, that player adds {G}{G}.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.ANY),
                new AwardManaToActivePlayerEffect(ManaColor.GREEN, 2));
    }
}
