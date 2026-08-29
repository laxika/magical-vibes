package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AugustaDeanOfOrder;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "155")
public class PlarggDeanOfChaos extends Card {

    public PlarggDeanOfChaos() {
        AugustaDeanOfOrder backFace = new AugustaDeanOfOrder();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect()),
                "{T}, Discard a card: Draw a card."));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{R}",
                List.of(new RevealUntilCardPredicateMayCastWithoutPayingManaEffect(
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                new CardMaxManaValuePredicate(3))))),
                "{4}{R}, {T}: Reveal cards from the top of your library until you reveal a nonlegendary, nonland card with mana value 3 or less. You may cast that card without paying its mana cost. Put all revealed cards not cast this way on the bottom of your library in a random order."));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Plargg, Dean of Chaos", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Augusta, Dean of Order", backFace.getEffects(EffectSlot.SPELL)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "AugustaDeanOfOrder";
    }
}
