package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "44")
public class KaitoCunningInfiltrator extends Card {

    public KaitoCunningInfiltrator() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new PutCountersOnSelfEffect(CounterType.LOYALTY)));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new MakeCreatureUnblockableEffect(),
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "+1: Up to one target creature you control can't be blocked this turn. Draw a card, then discard a card.",
                TargetFilters.creatureYouControl(),
                +1, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        "Ninja", 2, 1, CardColor.BLUE,
                        List.of(CardSubtype.NINJA), Set.of(), Set.of()
                )),
                "\u22122: Create a 2/1 blue Ninja creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(null, List.of(new CreateTokenEffect(
                                "Ninja", 2, 1, CardColor.BLUE,
                                List.of(CardSubtype.NINJA), Set.of(), Set.of()
                        )))),
                        "Whenever a player casts a spell, you create a 2/1 blue Ninja creature token."
                )),
                "\u22129: You get an emblem with \"Whenever a player casts a spell, you create a 2/1 blue Ninja creature token.\""
        ));
    }
}
