package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "312")
@CardRegistration(set = "DMR", collectorNumber = "237")
public class Umbilicus extends Card {

    public Umbilicus() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                UnlessPaysEffect.unlessPaysLife(Subjects.activePlayer(), 2, new BouncePermanentOnUpkeepEffect(
                        BouncePermanentOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER,
                        Set.of(),
                        "Choose a permanent to return to its owner's hand.")));
    }
}
