package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "152")
public class AngrathTheFlameChained extends Card {

    public AngrathTheFlameChained() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)
                ),
                "+1: Each opponent discards a card and loses 2 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect(3)
                ),
                "−3: Gain control of target creature until end of turn. Untap it. It gains haste until end of turn. Sacrifice it at the beginning of the next end step if it has mana value 3 or less.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect()),
                "−8: Each opponent loses life equal to the number of cards in their graveyard."
        ));
    }
}
