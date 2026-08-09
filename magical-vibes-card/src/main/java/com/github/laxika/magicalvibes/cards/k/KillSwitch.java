package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapAndLockOtherPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "133")
public class KillSwitch extends Card {

    public KillSwitch() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new TapAndLockOtherPermanentsEffect(new PermanentIsArtifactPredicate())),
                "{2}, {T}: Tap all other artifacts. They don't untap during their controllers' untap steps for as long as this artifact remains tapped."
        ));
    }
}
