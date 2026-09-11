package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.NontokenCreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "125")
public class VileRedeemer extends Card {

    private static final CreateTokenEffect SCION_TOKEN = new CreateTokenEffect(
            CardType.CREATURE,
            new NontokenCreatureDeathsThisTurn(CountScope.CONTROLLER),
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
                    "Sacrifice this token: Add {C}.")),
            false,
            false,
            false,
            0,
            Set.of());

    public VileRedeemer() {
        addEffect(EffectSlot.ON_SELF_CAST, new MayPayManaEffect(
                "{C}",
                SCION_TOKEN,
                "Pay {C} to create an Eldrazi Scion token for each nontoken creature that died under your control this turn?"));
    }
}
