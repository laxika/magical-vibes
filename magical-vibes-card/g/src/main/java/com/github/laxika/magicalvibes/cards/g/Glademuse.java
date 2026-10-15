package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "60")
public class Glademuse extends Card {

    public Glademuse() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                SpellCastTriggerEffect.whenCasterIsNotActiveTurn(
                        List.of(new DrawCardForTargetPlayerEffect(1))));
    }
}
