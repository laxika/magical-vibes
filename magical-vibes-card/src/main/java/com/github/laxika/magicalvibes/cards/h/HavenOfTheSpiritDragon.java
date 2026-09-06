package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "249")
public class HavenOfTheSpiritDragon extends Card {

    public HavenOfTheSpiritDragon() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        1, ManaSpendRestriction.SUBTYPE_CREATURE_SPELL, CardSubtype.DRAGON)),
                "{T}: Add one mana of any color. Spend this mana only to cast a Dragon creature spell."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.DRAGON),
                                        new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.PLANESWALKER),
                                                new CardSubtypePredicate(CardSubtype.UGIN))))))
                                .targetGraveyard(true)
                                .build()
                ),
                "{2}, {T}, Sacrifice this land: Return target Dragon creature card or Ugin planeswalker card from your graveyard to your hand."
        ));
    }
}
