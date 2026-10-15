package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCC", collectorNumber = "49")
@CardRegistration(set = "LCC", collectorNumber = "81")
public class FranciscoFowlMarauder extends Card {

    public FranciscoFowlMarauder() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // Whenever one or more Pirates you control deal damage to a player, Francisco explores.
        addEffect(EffectSlot.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE),
                        new ExploreEffect()));
    }
}
