package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OtherControlledCreaturesBecomeCopiesOfTopCreatureCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfLibraryOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "1")
public class DeceiverOfForm extends Card {

    public DeceiverOfForm() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                ConditionalEffect.unless(
                        new TopCardOfLibraryType(CardType.CREATURE),
                        new MayEffect(
                                new OtherControlledCreaturesBecomeCopiesOfTopCreatureCardUntilEndOfTurnEffect(),
                                "Have other creatures you control become copies of that card until end of turn?")),
                ConditionalEffect.unless(
                        new CardsInLibraryAtLeast(1),
                        new MayEffect(
                                new PutTopCardsOfLibraryOnBottomEffect(1),
                                "Put that card on the bottom of your library?"))));
    }
}
