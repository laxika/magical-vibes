package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRC", collectorNumber = "26")
@CardRegistration(set = "BRC", collectorNumber = "46")
public class TheArchimandrite extends Card {

    public TheArchimandrite() {
        PermanentHasAnySubtypePredicate advisorArtificerOrMonk =
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.ADVISOR,
                        CardSubtype.ARTIFICER,
                        CardSubtype.MONK));

        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, SequenceEffect.of(
                new BoostAllOwnCreaturesEffect(new EventValue(), new Fixed(0), advisorArtificerOrMonk),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES,
                        advisorArtificerOrMonk)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(3, advisorArtificerOrMonk),
                        new DrawCardEffect()
                ),
                "Tap three untapped Advisors, Artificers, and/or Monks you control: Draw a card."
        ));
    }
}
