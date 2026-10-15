package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "228")
public class RoxanneStarfallSavant extends Card {

    private static final CreateTokenEffect METEORITE = new CreateTokenEffect(
            CardType.ARTIFACT,
            1,
            "Meteorite",
            0,
            0,
            null,
            null,
            List.of(),
            Set.of(),
            Set.of(),
            false,
            true,
            Map.of(
                    EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(2),
                    EffectSlot.ON_SELF_TAPPED_FOR_MANA, new AddManaOfTypeProducedByTappedPermanentEffect()
            ),
            List.of(ManaAbilities.tapForAnyColor()),
            false,
            false,
            false,
            0,
            Set.of()
    );

    public RoxanneStarfallSavant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, METEORITE);
        addEffect(EffectSlot.ON_ATTACK, METEORITE);
    }
}
