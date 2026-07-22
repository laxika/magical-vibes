package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

/**
 * Howling Chorus — back face of Shrill Howler.
 * Creatures with power less than this creature's power can't block it.
 * Whenever this creature deals combat damage to a player, create a 3/2 colorless Eldrazi Horror creature token.
 */
public class HowlingChorus extends Card {

    public HowlingChorus() {
        // Creatures with power less than this creature's power can't block it.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());

        // Whenever this creature deals combat damage to a player, create a 3/2 colorless Eldrazi Horror
        // creature token.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new CreateTokenEffect(
                "Eldrazi Horror", 3, 2, null,
                List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), Set.of(), Set.of()));
    }
}
