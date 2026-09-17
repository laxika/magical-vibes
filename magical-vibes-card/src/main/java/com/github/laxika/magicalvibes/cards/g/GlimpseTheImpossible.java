package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLZ", collectorNumber = "58")
@CardRegistration(set = "SLZ", collectorNumber = "179")
@CardRegistration(set = "SLZ", collectorNumber = "300")
public class GlimpseTheImpossible extends Card {

    private static final CreateTokenEffect ELDRAZI_SPAWN_TOKEN = new CreateTokenEffect(
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

    public GlimpseTheImpossible() {
        addEffect(EffectSlot.SPELL,
                new ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect(
                        3, ELDRAZI_SPAWN_TOKEN));
    }
}
