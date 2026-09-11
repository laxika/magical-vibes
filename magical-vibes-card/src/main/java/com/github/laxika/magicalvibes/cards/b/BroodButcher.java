package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "199")
public class BroodButcher extends Card {

    private static final CreateTokenEffect ELDRAZI_SCION = new CreateTokenEffect(
            CardType.CREATURE,
            1,
            "Eldrazi Scion",
            1,
            1,
            null,
            null,
            List.of(CardSubtype.ELDRAZI, CardSubtype.SCION),
            Set.of(),
            Set.of(),
            false,
            false,
            Map.of(),
            List.of(new ActivatedAbility(
                    false,
                    null,
                    List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS)),
                    "Sacrifice this token: Add {C}."
            )),
            false,
            false,
            false,
            0,
            Set.of());

    public BroodButcher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ELDRAZI_SCION);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(new SacrificeCreatureCost(), new BoostTargetCreatureEffect(-2, -2)),
                "{B}{G}, Sacrifice a creature: Target creature gets -2/-2 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
