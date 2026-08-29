package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "11")
public class KorFirewalker extends Card {

    public KorFirewalker() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED)));

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED), List.of(new GainLifeEffect(1))),
                "Gain 1 life?"));
    }
}
