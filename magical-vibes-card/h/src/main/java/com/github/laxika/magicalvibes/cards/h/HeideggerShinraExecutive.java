package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCreaturesThanController;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "21")
@CardRegistration(set = "FIC", collectorNumber = "136")
public class HeideggerShinraExecutive extends Card {

    public HeideggerShinraExecutive() {
        PermanentCount soldiersYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER), CountScope.CONTROLLER);
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new BoostTargetCreatureEffect(soldiersYouControl, new Fixed(0)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                CreateTokenEffect.whiteSoldier(new OpponentsWithMoreCreaturesThanController()));
    }
}
