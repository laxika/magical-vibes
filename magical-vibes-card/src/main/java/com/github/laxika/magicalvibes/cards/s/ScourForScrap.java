package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "73")
public class ScourForScrap extends Card {

    public ScourForScrap() {
        CardTypePredicate artifact = new CardTypePredicate(CardType.ARTIFACT);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for an artifact card, reveal it, and put it into your hand",
                        new SearchLibraryEffect(artifact)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target artifact card from your graveyard to your hand",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(artifact)
                                .targetGraveyard(true)
                                .build())
        )));
    }
}
