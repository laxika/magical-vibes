package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToSliverCreatureCardsEffect;

@CardRegistration(set = "SLD", collectorNumber = "631")
public class DregscapeSliver extends Card {

    public DregscapeSliver() {
        addUnearth("{2}");
        addEffect(EffectSlot.STATIC,
                new GrantGraveyardAbilityToSliverCreatureCardsEffect(Card.unearthAbility("{2}")));
    }
}
