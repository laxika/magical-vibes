package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TopCardOfLibraryManaValue;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "96")
public class BanefulOmen extends Card {

    public BanefulOmen() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                        new LoseLifeEffect(new TopCardOfLibraryManaValue(), LoseLifeRecipient.EACH_OPPONENT)
                ), "Reveal the top card of your library?"));
    }
}
