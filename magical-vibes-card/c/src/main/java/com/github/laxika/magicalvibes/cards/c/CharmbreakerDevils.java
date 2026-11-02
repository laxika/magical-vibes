package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "134")
@CardRegistration(set = "IMA", collectorNumber = "120")
@CardRegistration(set = "C13", collectorNumber = "103")
@CardRegistration(set = "C15", collectorNumber = "145")
@CardRegistration(set = "C21", collectorNumber = "162")
@CardRegistration(set = "C20", collectorNumber = "147")
public class CharmbreakerDevils extends Card {

    public CharmbreakerDevils() {
        // At the beginning of your upkeep, return an instant or sorcery card at random
        // from your graveyard to your hand.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )))
                .returnAtRandom(true)
                .build());

        // Whenever you cast an instant or sorcery spell, Charmbreaker Devils gets +4/+0
        // until end of turn.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(new BoostSelfEffect(4, 0))
        ));
    }
}
