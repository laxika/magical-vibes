package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "16")
@CardRegistration(set = "4ED", collectorNumber = "13")
public class CircleOfProtectionArtifacts extends Card {

    public CircleOfProtectionArtifacts() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new PreventNextDamageFromChosenSourceMatchingEffect(new PermanentIsArtifactPredicate(), "artifact")),
                "The next time an artifact source of your choice would deal damage to you this turn, prevent that damage."
        ));
    }
}
