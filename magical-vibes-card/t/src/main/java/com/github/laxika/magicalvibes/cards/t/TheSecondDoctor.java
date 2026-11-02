package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawCardAndRestrictAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "156")
@CardRegistration(set = "WHO", collectorNumber = "440")
@CardRegistration(set = "WHO", collectorNumber = "553")
@CardRegistration(set = "WHO", collectorNumber = "761")
@CardRegistration(set = "WHO", collectorNumber = "1031")
@CardRegistration(set = "WHO", collectorNumber = "1144")
public class TheSecondDoctor extends Card {

    public TheSecondDoctor() {
        addStaticEffect(new PlayersHaveNoMaximumHandSizeEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new EachPlayerMayDrawCardAndRestrictAttackingEffect());
    }
}
