package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.ManaAbilities;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "33")
public class HydroManFluidFelon extends Card {

    public HydroManFluidFelon() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.withIntervening(
                        new CardColorPredicate(CardColor.BLUE),
                        List.of(new BoostSelfEffect(1, 1)),
                        new SourceIsCreature()));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.SELF),
                        new SetCardTypesUntilYourNextTurnEffect(Set.of(CardType.LAND)),
                        new GrantActivatedAbilityEffect(
                                ManaAbilities.tapFor(ManaColor.BLUE), GrantScope.SELF, null,
                                EffectDuration.UNTIL_YOUR_NEXT_TURN)));
    }
}
