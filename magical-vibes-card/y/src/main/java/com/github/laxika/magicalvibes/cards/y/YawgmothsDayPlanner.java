package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "319")
@CardRegistration(set = "MB2", collectorNumber = "555")
public class YawgmothsDayPlanner extends Card {

    public YawgmothsDayPlanner() {
        // {T}, Pay 2 life: Add {B}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(2), new AwardRestrictedManaEffect(
                        ManaColor.BLACK, 2, new ManaRestriction.ProducedByLifeLossAbility())),
                "{T}, Pay 2 life: Add {B}{B}."
        ));

        // You may cast spells from your graveyard. Only mana produced by abilities that caused you to lose life
        // may be spent to cast spells this way.
        addStaticEffect(new CastSpellsFromGraveyardEffect(new CardTruePredicate(), true));

        // If a card would be put into your graveyard from anywhere, exile it instead.
        addStaticEffect(new ExileOwnCardsInsteadOfGraveyardEffect());
    }
}
