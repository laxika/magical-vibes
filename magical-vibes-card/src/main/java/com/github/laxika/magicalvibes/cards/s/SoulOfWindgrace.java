package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "220")
public class SoulOfWindgrace extends Card {

    public SoulOfWindgrace() {
        MayEffect returnLand = new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardTypePredicate(CardType.LAND))
                        .enterTapped(true)
                        .build(),
                "Put a land card from a graveyard onto the battlefield tapped under your control?"
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, returnLand);
        addEffect(EffectSlot.ON_ATTACK, returnLand);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new GainLifeEffect(3)
                ),
                "{G}, Discard a land card: You gain 3 life."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new DrawCardEffect(1)
                ),
                "{1}{R}, Discard a land card: Draw a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF)
                ),
                "{2}{B}, Discard a land card: Soul of Windgrace gains indestructible until end of turn. Tap it."
        ));
    }
}
