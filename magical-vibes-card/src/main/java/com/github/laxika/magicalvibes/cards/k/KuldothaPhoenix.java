package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "SOM", collectorNumber = "95")
public class KuldothaPhoenix extends Card {

    public KuldothaPhoenix() {
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new MetalcraftConditionalEffect(
                        new MayPayManaEffect("{4}",
                                new ReturnCardFromGraveyardEffect(
                                        GraveyardChoiceDestination.BATTLEFIELD,
                                        new CardIsSelfPredicate(),
                                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                                        false, true, false, null, false
                                ),
                                "Pay {4} to return Kuldotha Phoenix to the battlefield?"
                        )
                )
        );
    }
}
