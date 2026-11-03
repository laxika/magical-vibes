package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "61")
@CardRegistration(set = "SLD", collectorNumber = "169")
@CardRegistration(set = "RVR", collectorNumber = "70")
@CardRegistration(set = "C14", collectorNumber = "139")
@CardRegistration(set = "DSC", collectorNumber = "368")
public class CryptGhast extends Card {

    public CryptGhast() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                new MayPayManaEffect("{W/B}",
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true),
                        "Pay {W/B} to extort?"));
        addTrigger(Triggers.playerTapsLandForMana(),
                new AddManaWhenLandOfSubtypeTappedForManaEffect(CardSubtype.SWAMP, ManaColor.BLACK, true));
    }
}
