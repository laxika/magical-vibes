package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "226")
public class VeteranBeastrider extends Card {

    public VeteranBeastrider() {
        // At the beginning of your end step, untap each creature you control.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate())));

        // {2}{G}{W}: Creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{W}",
                List.of(BoostEffect.creaturesYouControl(1, 1)),
                "{2}{G}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
