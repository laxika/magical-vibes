package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealForManaAndPutIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceManaDrainWithColorlessEffect;

@CardRegistration(set = "MOM", collectorNumber = "249")
public class OmnathLocusOfAll extends Card {

    public OmnathLocusOfAll() {
        addEffect(EffectSlot.STATIC, new ReplaceManaDrainWithColorlessEffect(ManaColor.BLACK));
        addEffect(EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED,
                new LookAtTopCardMayRevealForManaAndPutIntoHandEffect());
    }
}
