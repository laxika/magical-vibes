package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "221")
public class KastralTheWindcrested extends Card {

    public KastralTheWindcrested() {
        CardAllOfPredicate birdCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.BIRD)));
        ChooseOneEffect chooseOne = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a Bird creature card onto the battlefield with a finality counter on it",
                        new MayEffect(
                                new PutCardFromHandOrGraveyardOntoBattlefieldEffect(
                                        birdCreature, "Bird creature", CounterType.FINALITY),
                                "Put a Bird creature card from your hand or graveyard onto the battlefield with a finality counter on it?")),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on each Bird you control",
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.BIRD))),
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1))));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.BIRD), chooseOne, false, true));
    }
}
