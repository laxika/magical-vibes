package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.AddOneCounterToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1050")
public class PirImaginativeRascal extends Card {

    private static final String TOOTHY = "Toothy, Imaginary Friend";

    public PirImaginativeRascal() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(TOOTHY),
                "Have target player put Toothy into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addStaticEffect(new AddOneCounterToControlledPermanentsEffect(false));
    }
}
