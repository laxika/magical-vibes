package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;

@CardRegistration(set = "SPM", collectorNumber = "33")
@CardRegistration(set = "SPM", collectorNumber = "250")
@CardRegistration(set = "OM1", collectorNumber = "27")
public class HydroManFluidFelon extends Card {

    public HydroManFluidFelon() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsCreature()),
                new BoostSelfEffect(1, 1));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new UntapPermanentsEffect(Subjects.source()),
                        new SetCardTypesEffect(Set.of(CardType.LAND), Subjects.source(),
                                Duration.UNTIL_YOUR_NEXT_TURN),
                        new GrantActivatedAbilityEffect(
                                ManaAbilities.tapFor(ManaColor.BLUE), Subjects.source(), null,
                                Duration.UNTIL_YOUR_NEXT_TURN)));
    }
}
