package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipAllExtraTurnsReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "17")
@CardRegistration(set = "DMC", collectorNumber = "93")
public class GerrardsHourglassPendant extends Card {

    public GerrardsHourglassPendant() {
        addEffect(EffectSlot.STATIC, new SkipAllExtraTurnsReplacementEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new ExileSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.ENCHANTMENT),
                                        new CardTypePredicate(CardType.LAND))))
                                .returnAll(true)
                                .fromBattlefieldThisTurn(true)
                                .enterTapped(true)
                                .build()),
                "{4}, {T}, Exile Gerrard's Hourglass Pendant: Return to the battlefield tapped all artifact, creature, enchantment, and land cards in your graveyard that were put there from the battlefield this turn."
        ));
    }
}
