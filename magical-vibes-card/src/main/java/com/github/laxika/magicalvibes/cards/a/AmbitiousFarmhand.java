package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeasonedCathar;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "8")
public class AmbitiousFarmhand extends Card {

    public AmbitiousFarmhand() {
        SeasonedCathar backFace = new SeasonedCathar();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When this creature enters, you may search your library for a basic Plains card,
        // reveal it, put it into your hand, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.BASIC),
                                new CardSubtypePredicate(CardSubtype.PLAINS)))),
                        "Search your library for a basic Plains card?"));

        // Coven — {1}{W}{W}: Transform this creature. Activate only if you control three or
        // more creatures with different powers.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{W}",
                List.of(new TransformSelfEffect()),
                "Coven — {1}{W}{W}: Transform this creature. Activate only if you control three or more creatures with different powers.",
                ActivationTimingRestriction.COVEN
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "SeasonedCathar";
    }
}
