package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "35")
public class RuneOfProtectionArtifacts extends Card {

    public RuneOfProtectionArtifacts() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYou(new PermanentIsArtifactPredicate(), "artifact")),
                "The next time an artifact source of your choice would deal damage to you this turn, prevent that damage."
        ));
        addCycling("{2}");
    }
}
