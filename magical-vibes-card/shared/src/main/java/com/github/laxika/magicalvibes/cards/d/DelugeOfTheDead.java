package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;

import java.util.List;

/**
 * Back face of Invasion of Innistrad.
 */
public class DelugeOfTheDead extends Card {

    public DelugeOfTheDead() {
        // When this enchantment enters, create two 2/2 black Zombie creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.blackZombie(2));

        // {2}{B}: Exile target card from a graveyard. If it was a creature card, create a 2/2
        // black Zombie creature token.
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}",
                List.of(new ExileGraveyardCardCreateTokenIfCreatureEffect()),
                "Exile target card from a graveyard. If it was a creature card, create a 2/2 black Zombie creature token."));
    }
}
