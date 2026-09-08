package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnAllyCreatureEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class LukkaWaywardBonder extends Card {

    private static final String EMBLEM_TEXT =
            "Whenever a creature you control enters, it deals damage equal to its power to any target.";

    public LukkaWaywardBonder() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MayEffect(new DiscardCardThenEffect(
                        null, new DrawCardEffect(1), "a card", CardType.CREATURE,
                        new DrawCardEffect(2)), "Discard a card?")),
                "+1: You may discard a card. If you do, draw a card. If a creature card was discarded this way, draw two cards instead."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .grantHaste(true)
                        .exileAtNextUpkeep(true)
                        .build()),
                "−2: Return target creature card from your graveyard to the battlefield. It gains haste. Exile it at the beginning of your next upkeep."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnAllyCreatureEntersEffect.Marker()),
                        EMBLEM_TEXT)),
                "−7: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
