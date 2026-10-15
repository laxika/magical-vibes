package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "61")
@CardRegistration(set = "SLD", collectorNumber = "169")
@CardRegistration(set = "RVR", collectorNumber = "70")
@CardRegistration(set = "C14", collectorNumber = "139")
@CardRegistration(set = "DSC", collectorNumber = "368")
public class CryptGhast extends Card {

    public CryptGhast() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new MayPayManaEffect("{W/B}",
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true),
                        "Pay {W/B} to extort?"))));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaWhenLandOfSubtypeTappedForManaEffect(CardSubtype.SWAMP, ManaColor.BLACK, true));
    }
}
