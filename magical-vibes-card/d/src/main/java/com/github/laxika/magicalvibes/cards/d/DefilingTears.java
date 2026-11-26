package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "99")
public class DefilingTears extends Card {

    public DefilingTears() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLACK))
                .addSpellEffect(BoostEffect.target(1, -1))
                .addSpellEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                                "{B}: Regenerate this creature."),
                        Subjects.target(),
                        null,
                        Duration.UNTIL_END_OF_TURN
                ));
    }
}
