package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCreatureCardsOfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToSliverCreatureCardsEffect;

@CardRegistration(set = "SLD", collectorNumber = "631")
@CardRegistration(set = "MH1", collectorNumber = "88")
public class DregscapeSliver extends Card {

    public DregscapeSliver() {
        addEffect(EffectSlot.STATIC, new GrantGraveyardAbilityToCreatureCardsOfSubtypeEffect(
                Card.unearthAbility("{2}"), CardSubtype.SLIVER));
        addUnearth("{2}");
    }
}
