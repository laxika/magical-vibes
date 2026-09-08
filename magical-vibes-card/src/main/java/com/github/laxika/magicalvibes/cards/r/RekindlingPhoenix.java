package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "111")
public class RekindlingPhoenix extends Card {

    private static final String NAME = "Rekindling Phoenix";

    public RekindlingPhoenix() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1,
                "Elemental",
                0,
                1,
                CardColor.RED,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.UPKEEP_TRIGGERED, new SacrificeSelfThenEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardNamedPredicate(NAME))
                                .targetGraveyard(true)
                                .grantHaste(true)
                                .build()
                ))
        ));
    }
}
