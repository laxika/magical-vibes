package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "60")
@CardRegistration(set = "USG", collectorNumber = "60")
public class ArcaneLaboratory extends Card {

    public ArcaneLaboratory() {
        addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1, SpellLimitScope.EACH_PLAYER));
    }
}
