package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantActivateAbilitiesOfGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromGraveyardsEffect;

@CardRegistration(set = "XLN", collectorNumber = "2")
public class AshesOfTheAbhorrent extends Card {

    public AshesOfTheAbhorrent() {
        // Players can't cast spells from graveyards or activate abilities of cards in graveyards.
        addEffect(EffectSlot.STATIC, new PlayersCantCastSpellsFromGraveyardsEffect());
        addEffect(EffectSlot.STATIC, new PlayersCantActivateAbilitiesOfGraveyardCardsEffect());

        // Whenever a creature dies, you gain 1 life.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new GainLifeEffect(1));
    }
}
