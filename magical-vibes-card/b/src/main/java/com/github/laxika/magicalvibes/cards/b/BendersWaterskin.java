package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;

@CardRegistration(set = "TLA", collectorNumber = "255")
public class BendersWaterskin extends Card {

    public BendersWaterskin() {
        addStaticEffect(new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                TurnStep.UNTAP, null, TapUntapScope.SELF));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
