package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "82")
@CardRegistration(set = "5ED", collectorNumber = "102")
public class MagusOfTheUnseen extends Card {

    public MagusOfTheUnseen() {
        // {1}{U}, {T}: Untap target artifact an opponent controls and gain control of it until end
        // of turn. It gains haste until end of turn. When you lose control of the artifact, tap it.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{U}",
                List.of(
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN, true),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{1}{U}, {T}: Untap target artifact an opponent controls and gain control of it until end of turn. It gains haste until end of turn. When you lose control of the artifact, tap it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be an artifact an opponent controls")));
    }
}
