package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "65")
public class HellkiteIgniter extends Card {

    public HellkiteIgniter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostSelfPerControlledPermanentEffect(1, 0, new PermanentIsArtifactPredicate())),
                "{1}{R}: Hellkite Igniter gets +X/+0 until end of turn, where X is the number of artifacts you control."
        ));
    }
}
