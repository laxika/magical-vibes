package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "86")
@CardRegistration(set = "TMT", collectorNumber = "234")
public class BroadcastTakeover extends Card {

    public BroadcastTakeover() {
        PermanentPredicate opponentArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        addEffect(EffectSlot.SPELL, new GainControlOfAllPermanentsMatchingEffect(
                opponentArtifact,
                ControlDuration.END_OF_TURN,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                )));
    }
}
