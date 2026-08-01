package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "VIS", collectorNumber = "134")
public class RighteousWar extends Card {

    public RighteousWar() {
        // White creatures you control have protection from black.
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)),
                GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));

        // Black creatures you control have protection from white.
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)),
                GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
    }
}
