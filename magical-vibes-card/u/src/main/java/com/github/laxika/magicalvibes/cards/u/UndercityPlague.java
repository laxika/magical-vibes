package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "GTC", collectorNumber = "83")
public class UndercityPlague extends Card {

    public UndercityPlague() {
        addSpellEffect(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.TARGET_PLAYER));
        addSpellEffect(new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
