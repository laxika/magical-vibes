package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SpiresOfOrazca;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "249")
public class ThaumaticCompass extends Card {

    public ThaumaticCompass() {
        // Set up back face
        SpiresOfOrazca backFace = new SpiresOfOrazca();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {3}, {T}: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(new SearchLibraryForBasicLandToHandEffect()),
                "{3}, {T}: Search your library for a basic land card, reveal it, put it into your hand, then shuffle."
        ));

        // At the beginning of your end step, if you control seven or more lands, transform this artifact.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ControlsPermanentCountConditionalEffect(7,
                        new PermanentIsLandPredicate(),
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpiresOfOrazca";
    }
}
