package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "6")
@CardRegistration(set = "SLD", collectorNumber = "1030")
@CardRegistration(set = "SLD", collectorNumber = "2246")
@CardRegistration(set = "RVR", collectorNumber = "9")
@CardRegistration(set = "RVR", collectorNumber = "303")
@CardRegistration(set = "WOT", collectorNumber = "1")
@CardRegistration(set = "C16", collectorNumber = "59")
@CardRegistration(set = "C17", collectorNumber = "57")
public class BlindObedience extends Card {

    public BlindObedience() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE), true));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null, List.of(new MayPayManaEffect("{W/B}",
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true),
                        "Pay {W/B} to extort?"))));
    }
}
