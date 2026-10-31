package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "NPH", collectorNumber = "164")
@CardRegistration(set = "BRR", collectorNumber = "61")
@CardRegistration(set = "C18", collectorNumber = "228")
public class UnwindingClock extends Card {

    public UnwindingClock() {
        addStaticEffect(new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                TurnStep.UNTAP, new PermanentIsArtifactPredicate()));
    }
}
