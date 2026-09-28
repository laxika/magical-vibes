package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEncoreToCreatureCardsOfSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "36")
@CardRegistration(set = "OTC", collectorNumber = "72")
public class GraywatersFixer extends Card {

    public GraywatersFixer() {
        addEffect(EffectSlot.STATIC, new GrantEncoreToCreatureCardsOfSubtypesEffect(Set.of(
                CardSubtype.ASSASSIN,
                CardSubtype.MERCENARY,
                CardSubtype.PIRATE,
                CardSubtype.ROGUE,
                CardSubtype.WARLOCK)));
    }
}
