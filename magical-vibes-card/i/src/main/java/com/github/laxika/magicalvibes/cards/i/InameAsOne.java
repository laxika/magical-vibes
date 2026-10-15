package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "151")
public class InameAsOne extends Card {

    public InameAsOne() {
        CardAllOfPredicate spiritPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardSubtypePredicate(CardSubtype.SPIRIT)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastFromZone(Zone.HAND),
                new MayEffect(
                        new SearchLibraryEffect(spiritPermanent, LibrarySearchDestination.BATTLEFIELD),
                        "Search your library for a Spirit permanent card?")));

        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                SequenceEffect.of(
                        new ExileSourceCardFromGraveyardEffect(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(spiritPermanent)
                                .targetGraveyard(true)
                                .build()),
                "Exile Iname as One to return target Spirit permanent card?"));
    }
}
