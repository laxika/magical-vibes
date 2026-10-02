package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantChosenCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMKM", collectorNumber = "21")
public class WoodlandInvestigation extends Card {

    public WoodlandInvestigation() {
        addEffect(EffectSlot.SPELL, new SearchLibraryAndConditionalEffect(
                CardPredicateUtils.basicLand(),
                LibrarySearchDestination.HAND,
                new CardTruePredicate(),
                new PerpetuallyGrantChosenCardCharacteristicsEffect(
                        Set.of(CardType.ARTIFACT),
                        Set.of(CardSubtype.CLUE),
                        List.of(new ActivatedAbility(
                                false,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                                "{2}, Sacrifice this permanent: Draw a card.")))));
    }
}
