package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "191")
public class SarkhanVol extends Card {

    public SarkhanVol() {
        // +1: Creatures you control get +1/+1 and gain haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES)
                ),
                "+1: Creatures you control get +1/+1 and gain haste until end of turn."
        ));

        // −2: Gain control of target creature until end of turn. Untap that creature. It gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                ),
                "−2: Gain control of target creature until end of turn. Untap that creature. It gains haste until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // −6: Create five 4/4 red Dragon creature tokens with flying.
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateTokenEffect(5, "Dragon", 4, 4,
                        CardColor.RED, List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), Set.of())),
                "−6: Create five 4/4 red Dragon creature tokens with flying."
        ));
    }
}
