package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;

import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "171")
public class AbstractPaintmage extends Card {

    public AbstractPaintmage() {
        // At the beginning of your first main phase, add {U}{R}.
        // Spend this mana only to cast instant and sorcery spells.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new AwardRestrictedManaEffect(ManaColor.BLUE, 1, Set.of(CardType.INSTANT, CardType.SORCERY)));
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new AwardRestrictedManaEffect(ManaColor.RED, 1, Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
