package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OtherControlledCreaturesBecomeCopiesOfTopCreatureCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfLibraryOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "OGW", collectorNumber = "1")
public class DeceiverOfForm extends Card {

    public DeceiverOfForm() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                ConditionalEffect.unless(
                        new TopCardOfLibraryType(CardType.CREATURE),
                        new MayEffect(
                                new OtherControlledCreaturesBecomeCopiesOfTopCreatureCardUntilEndOfTurnEffect(),
                                "Have other creatures you control become copies of that card until end of turn?")),
                new MayEffect(
                        new PutTopCardsOfLibraryOnBottomEffect(1),
                        "Put that card on the bottom of your library?")));
    }
}
