package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "19")
public class LightwielderPaladin extends Card {

    public LightwielderPaladin() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new ExilePermanentDamagedPlayerControlsEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED))),
                        "You may exile target black or red permanent that player controls."));
    }
}
