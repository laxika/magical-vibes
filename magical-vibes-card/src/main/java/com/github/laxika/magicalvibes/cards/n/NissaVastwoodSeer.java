package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "189")
public class NissaVastwoodSeer extends Card {

    public NissaVastwoodSeer() {
        setBackFaceCard(new NissaSageAnimist());

        // When Nissa enters, you may search your library for a basic Forest card, reveal it,
        // put it into your hand, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.BASIC),
                        new CardSubtypePredicate(CardSubtype.FOREST)))),
                "Search your library for a basic Forest card?"));

        // Whenever a land you control enters, if you control seven or more lands, exile Nissa,
        // then return her to the battlefield transformed under her owner's control.
        // The land that triggered this has already entered, so it counts toward the seven.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(7, new PermanentIsLandPredicate()),
                new ExileSelfAndReturnTransformedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "NissaSageAnimist";
    }
}
