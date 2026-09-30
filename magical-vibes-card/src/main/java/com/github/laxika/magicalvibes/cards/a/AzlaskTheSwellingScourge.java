package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "5")
@CardRegistration(set = "M3C", collectorNumber = "9")
@CardRegistration(set = "M3C", collectorNumber = "17")
@CardRegistration(set = "M3C", collectorNumber = "25")
@CardRegistration(set = "M3C", collectorNumber = "136")
public class AzlaskTheSwellingScourge extends Card {

    public AzlaskTheSwellingScourge() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardIsColorlessPredicate(), new ExperienceCountersEffect(1)));
        addEffect(EffectSlot.ON_DEATH, new ExperienceCountersEffect(1));

        PermanentHasAnySubtypePredicate scionsAndSpawns = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.SCION, CardSubtype.SPAWN));
        ControllerExperienceCounters experienceCounters = new ControllerExperienceCounters();
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(
                        new BoostAllOwnCreaturesEffect(experienceCounters, experienceCounters),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES,
                                scionsAndSpawns),
                        new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                                EffectSlot.ON_ATTACK,
                                new SacrificePermanentsEffect(
                                        1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER),
                                scionsAndSpawns)),
                "{W}{U}{B}{R}{G}: Creatures you control get +X/+X until end of turn, where X is the number "
                        + "of experience counters you have. Scions and Spawns you control gain indestructible "
                        + "and annihilator 1 until end of turn."
        ));
    }
}
