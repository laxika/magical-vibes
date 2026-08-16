package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "215")
@CardRegistration(set = "M10", collectorNumber = "145")
@CardRegistration(set = "M11", collectorNumber = "147")
@CardRegistration(set = "M12", collectorNumber = "148")
@CardRegistration(set = "M14", collectorNumber = "143")
@CardRegistration(set = "M15", collectorNumber = "154")
@CardRegistration(set = "M19", collectorNumber = "150")
@CardRegistration(set = "9ED", collectorNumber = "200")
@CardRegistration(set = "POR", collectorNumber = "137")
@CardRegistration(set = "P02", collectorNumber = "107")
@CardRegistration(set = "8ED", collectorNumber = "197")
@CardRegistration(set = "7ED", collectorNumber = "199")
@CardRegistration(set = "ULG", collectorNumber = "84")
@CardRegistration(set = "S99", collectorNumber = "111")
public class LavaAxe extends Card {

    public LavaAxe() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PLAYER));
    }
}
