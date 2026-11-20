package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SCG", collectorNumber = "116")
public class DivergentGrowth extends Card {

    public DivergentGrowth() {
        addSpellEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(),
                Subjects.landsYouControl(),
                null,
                Duration.UNTIL_END_OF_TURN
        ));
    }
}
