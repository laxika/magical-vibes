package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "63")
public class ChitteringDispatcher extends Card {

    private static final CreateTokenEffect SPAWN_TOKEN = new CreateTokenEffect(
            CardType.CREATURE,
            1,
            "Eldrazi Spawn",
            0,
            1,
            null,
            null,
            List.of(CardSubtype.ELDRAZI, CardSubtype.SPAWN),
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

    public ChitteringDispatcher() {
        // Myriad
        addEffect(EffectSlot.ON_ATTACK,
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());

        // When this creature leaves the battlefield, create a 0/1 colorless Eldrazi Spawn creature token
        // with "Sacrifice this token: Add {C}."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, SPAWN_TOKEN);
    }
}
