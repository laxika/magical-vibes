package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "245")
@CardRegistration(set = "CMM", collectorNumber = "546")
public class NestingDragon extends Card {

    public NestingDragon() {
        // Landfall — Whenever a land you control enters, create a 0/2 red Dragon Egg creature
        // token with defender. When that token dies, create a 2/2 red Dragon creature token with
        // flying and firebreathing.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Dragon Egg",
                0,
                2,
                CardColor.RED,
                null,
                List.of(CardSubtype.EGG),
                Set.of(Keyword.DEFENDER),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.ON_DEATH, dragonToken()),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }

    private static CreateTokenEffect dragonToken() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Dragon",
                2,
                2,
                CardColor.RED,
                null,
                List.of(CardSubtype.DRAGON),
                Set.of(Keyword.FLYING),
                Set.of(),
                false,
                false,
                Map.<EffectSlot, CardEffect>of(),
                List.of(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                        "{R}: This token gets +1/+0 until end of turn.")),
                false,
                false,
                false,
                0,
                Set.of()
        );
    }
}
