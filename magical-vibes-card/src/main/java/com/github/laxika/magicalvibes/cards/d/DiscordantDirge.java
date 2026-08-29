package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "131")
public class DiscordantDirge extends Card {

    public DiscordantDirge() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.VERSE),
                "Put a verse counter on Discordant Dirge?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new ChooseCardsFromTargetHandEffect(
                                new CountersOnSource(CounterType.VERSE), List.of(), List.of(),
                                HandChoiceDestination.DISCARD, false, null, true, false)
                ),
                "{B}, Sacrifice this enchantment: Look at target opponent's hand and choose up to X cards from it, "
                        + "where X is the number of verse counters on this enchantment. That player discards those cards.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
