package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "11")
@CardRegistration(set = "A25", collectorNumber = "21")
public class KorFirewalker extends Card {

    public KorFirewalker() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED)));

        addTrigger(Triggers.playerCastsSpell(), new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED), List.of(new GainLifeEffect(1))),
                "Gain 1 life?"));
    }
}
