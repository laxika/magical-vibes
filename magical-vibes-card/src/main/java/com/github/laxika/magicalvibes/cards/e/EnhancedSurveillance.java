package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalSurveilCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "40")
public class EnhancedSurveillance extends Card {

    public EnhancedSurveillance() {
        addEffect(EffectSlot.STATIC, new AdditionalSurveilCardsEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileSelfCost(), new ShuffleGraveyardIntoLibraryEffect(false)),
                "Exile Enhanced Surveillance: Shuffle your graveyard into your library."
        ));
    }
}
