package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToPlayersFromColorSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.a.AkkiLavarunner}.
 */
public class TokTokVolcanoBorn extends Card {

    public TokTokVolcanoBorn() {
        // "Protection from red"
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED)));

        // "If a red source would deal damage to a player, it deals that much damage plus 1 to that
        // player instead." - every player is affected, whoever controls the source.
        addStaticEffect(new AdditionalDamageToPlayersFromColorSourcesEffect(Set.of(CardColor.RED), 1));
    }
}
