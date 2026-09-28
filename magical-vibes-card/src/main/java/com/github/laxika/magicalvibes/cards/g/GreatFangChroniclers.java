package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "18")
public class GreatFangChroniclers extends Card {

    public GreatFangChroniclers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new ConjureCardToBattlefieldEffect("Muraganda Petroglyphs")),
                "{3}{G}: Conjure a card named Muraganda Petroglyphs onto the battlefield, then this creature loses this ability. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withMaxActivationsPerGame(1));
    }
}
