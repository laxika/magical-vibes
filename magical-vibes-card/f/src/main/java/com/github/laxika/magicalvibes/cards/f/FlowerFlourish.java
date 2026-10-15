package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "226")
public class FlowerFlourish extends Card {

    public FlowerFlourish() {
        CardPredicate basicForest = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardSubtypePredicate(CardSubtype.FOREST)));
        CardPredicate basicPlains = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardSubtypePredicate(CardSubtype.PLAINS)));
        CardEffect flower = new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(basicForest, basicPlains)));
        CardEffect flourish = new BoostAllOwnCreaturesEffect(2, 2);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Flower — Search your library for a basic Forest or Plains card, reveal it, put it into your hand, then shuffle",
                        flower).withManaCost("{G/W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Flourish — Creatures you control get +2/+2 until end of turn",
                        flourish).withManaCost("{4}{G}{W}")
        )));
    }
}
