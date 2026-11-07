package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOwnCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "718")
@CardRegistration(set = "CMM", collectorNumber = "751")
@CardRegistration(set = "MKC", collectorNumber = "53")
public class UginsMastery extends Card {

    public UginsMastery() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardIsColorlessPredicate(),
                                new CardTypePredicate(CardType.CREATURE)))),
                new ManifestTopCardEffect());
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new AttackingCreaturesTotalPowerAtLeast(6)),
                new MayEffect(new TurnOwnCreatureFaceUpEffect(), "Turn a face-down creature you control face up?"));
    }
}
