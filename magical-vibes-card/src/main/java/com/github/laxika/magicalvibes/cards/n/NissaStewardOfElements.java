package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardPutLandOrCreatureWithinLoyaltyEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "204")
public class NissaStewardOfElements extends Card {

    public NissaStewardOfElements() {
        // Enters with X loyalty counters (printed loyalty "X"); handled by the engine off the {X} cost.

        // +2: Scry 2.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new ScryEffect(2)),
                "+2: Scry 2."
        ));

        // 0: Look at the top card of your library. If it's a land card or a creature card with mana
        //    value less than or equal to the number of loyalty counters on Nissa, you may put that
        //    card onto the battlefield.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new LookAtTopCardPutLandOrCreatureWithinLoyaltyEffect()),
                "0: Look at the top card of your library. If it's a land card or a creature card with "
                        + "mana value less than or equal to the number of loyalty counters on Nissa, "
                        + "you may put that card onto the battlefield."
        ));

        // −6: Untap up to two target lands you control. They become 5/5 Elemental creatures with
        //     flying and haste until end of turn. They're still lands.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS),
                        new AnimatePermanentsEffect(
                                5, 5, List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.FLYING, Keyword.HASTE), null, Set.of(),
                                GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN)
                ),
                "−6: Untap up to two target lands you control. They become 5/5 Elemental "
                        + "creatures with flying and haste until end of turn. They're still lands.",
                null, -6, null, null,
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentIsLandPredicate(), "Target must be a land you control"),
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentIsLandPredicate(), "Target must be a land you control")
                ),
                0, 2
        ));
    }
}
