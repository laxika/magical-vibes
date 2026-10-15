package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "147")
@CardRegistration(set = "PIP", collectorNumber = "439")
@CardRegistration(set = "PIP", collectorNumber = "675")
@CardRegistration(set = "PIP", collectorNumber = "967")
public class DiamondCity extends Card {

    public DiamondCity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MoveCounterFromSourceToTargetCreatureEffect(CounterType.SHIELD)),
                "{T}: Move a shield counter from this land onto target creature. Activate only if two or more creatures entered the battlefield under your control this turn.",
                TargetFilters.creature()
        ).withActivationCondition(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE), 2),
                "Activate only if two or more creatures entered the battlefield under your control this turn."));
    }
}
