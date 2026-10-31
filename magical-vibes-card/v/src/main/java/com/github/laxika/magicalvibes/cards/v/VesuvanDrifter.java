package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTopCreatureCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.Set;

@CardRegistration(set = "MAT", collectorNumber = "10")
@CardRegistration(set = "MAT", collectorNumber = "60")
@CardRegistration(set = "MAT", collectorNumber = "110")
@CardRegistration(set = "MAT", collectorNumber = "156")
@CardRegistration(set = "MAT", collectorNumber = "194")
public class VesuvanDrifter extends Card {

    public VesuvanDrifter() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                        new BecomeCopyOfTopCreatureCardUntilEndOfTurnEffect(Set.of(Keyword.FLYING))),
                "Reveal the top card of your library?")
        );
    }
}
