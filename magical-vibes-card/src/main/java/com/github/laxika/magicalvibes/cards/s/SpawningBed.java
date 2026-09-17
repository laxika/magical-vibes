package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "PIO", collectorNumber = "277")
public class SpawningBed extends Card {

    private static final CreateTokenEffect ELDRAZI_SCION = new CreateTokenEffect(
            CardType.CREATURE,
            3,
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

    public SpawningBed() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {6}, {T}, Sacrifice this land: Create three 1/1 colorless Eldrazi Scion creature
        // tokens. They have "Sacrifice this token: Add {C}."
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new SacrificeSelfCost(), ELDRAZI_SCION),
                "{6}, {T}, Sacrifice this land: Create three 1/1 colorless Eldrazi Scion creature "
                        + "tokens. They have \"Sacrifice this token: Add {C}.\""
        ));
    }
}
