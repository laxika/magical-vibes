package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "237")
@CardRegistration(set = "AKR", collectorNumber = "281")
public class ThroneOfTheGodPharaoh extends Card {

    public ThroneOfTheGodPharaoh() {
        // At the beginning of your end step, each opponent loses life equal to the
        // number of tapped creatures you control.
        PermanentCount tappedCreaturesYouControl = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                )),
                CountScope.CONTROLLER);
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new LoseLifeEffect(tappedCreaturesYouControl, LoseLifeRecipient.EACH_OPPONENT));
    }
}
