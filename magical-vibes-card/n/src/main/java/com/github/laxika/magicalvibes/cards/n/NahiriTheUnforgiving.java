package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackPlayerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourceLoyaltyPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "211")
public class NahiriTheUnforgiving extends Card {

    private static final CardAnyOfPredicate CREATURE_OR_EQUIPMENT = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
    private static final CardAllOfPredicate GRAVEYARD_TARGET = new CardAllOfPredicate(List.of(
            CREATURE_OR_EQUIPMENT,
            new CardManaValueLessThanSourceLoyaltyPredicate()));

    public NahiriTheUnforgiving() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TargetCreatureMustAttackPlayerUntilNextTurnEffect()),
                "+1: Until your next turn, up to one target creature attacks a player each combat if able.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()),
                0, 1));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DiscardAndDrawCardEffect()),
                "+1: Discard a card, then draw a card."));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        GRAVEYARD_TARGET,
                        true,
                        List.of(),
                        true,
                        true)),
                "0: Exile target creature or Equipment card with mana value less than Nahiri's loyalty "
                        + "from your graveyard. Create a token that's a copy of it. That token gains haste. "
                        + "Exile it at the beginning of the next end step."));
    }
}
