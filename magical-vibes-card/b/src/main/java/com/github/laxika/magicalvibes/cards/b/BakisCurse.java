package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "HML", collectorNumber = "22")
public class BakisCurse extends Card {

    public BakisCurse() {
        // Per-creature amount: each creature takes 2 damage for each Aura attached to it,
        // so a creature with no Auras takes none.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new Scaled(new AttachmentsOnSource(true, false), 2), false, false, null, true));
    }
}
