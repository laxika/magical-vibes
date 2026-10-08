package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCC", collectorNumber = "222")
public class CoerciveRecruiter extends Card {

    public CoerciveRecruiter() {
        // Whenever this creature or another Pirate you control enters, gain control of target
        // creature until end of turn. Untap that creature. Until end of turn, it gains haste and
        // becomes a Pirate in addition to its other types.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, gainControl());
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.PIRATE), gainControl()));
    }

    private static SequenceEffect gainControl() {
        return SequenceEffect.of(
                new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                new UntapPermanentsEffect(TapUntapScope.TARGET),
                new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.PIRATE, GrantScope.TARGET),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
