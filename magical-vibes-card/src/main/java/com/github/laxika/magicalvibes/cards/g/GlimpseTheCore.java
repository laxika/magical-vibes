package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "186")
public class GlimpseTheCore extends Card {

    public GlimpseTheCore() {
        CardPredicate basicForest = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardSubtypePredicate(CardSubtype.FOREST)));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a basic Forest card, put that card onto the battlefield tapped, then shuffle",
                        new SearchLibraryEffect(basicForest, LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Cave card from your graveyard to the battlefield tapped",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardSubtypePredicate(CardSubtype.CAVE))
                                .targetGraveyard(true)
                                .enterTapped(true)
                                .build())
        )));
    }
}
