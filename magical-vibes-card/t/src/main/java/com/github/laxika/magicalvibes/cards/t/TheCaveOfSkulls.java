package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScavengeEqualToManaCostToCreatureCardsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "573")
public class TheCaveOfSkulls extends Card {

    public TheCaveOfSkulls() {
        // Each creature card in your graveyard has scavenge. The scavenge cost is equal to its mana cost.
        addEffect(EffectSlot.STATIC, new GrantScavengeEqualToManaCostToCreatureCardsEffect());

        // Whenever chaos ensues, create two 1/1 white Warrior creature tokens.
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
