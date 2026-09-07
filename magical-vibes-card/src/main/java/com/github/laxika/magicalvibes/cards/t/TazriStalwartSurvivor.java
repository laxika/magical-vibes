package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasNonManaActivatedAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "6")
public class TazriStalwartSurvivor extends Card {

    public TazriStalwartSurvivor() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.CREATURE_COLORS_ABILITIES)),
                        "{T}: Add one mana of any of this creature's colors. Spend this mana only to activate an ability of a creature.")
                        .withRequiresAnotherActivatedAbility(),
                GrantScope.ALL_OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{U}{B}{R}{G}",
                List.of(new MillControllerAndReturnMilledCardsToHandEffect(
                        5,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardHasNonManaActivatedAbilityPredicate())))),
                "{W}{U}{B}{R}{G}, {T}: Mill five cards. Put all creature cards with activated abilities that aren't mana abilities from among the milled cards into your hand."));
    }
}
