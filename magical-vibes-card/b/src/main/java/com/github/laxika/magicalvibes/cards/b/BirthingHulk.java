package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "121")
public class BirthingHulk extends Card {

    public BirthingHulk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE,
                2,
                "Eldrazi Scion",
                1,
                1,
                null,
                Set.of(),
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
                Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{C}",
                List.of(new RegenerateEffect()),
                "{1}{C}: Regenerate this creature."
        ));
    }
}
