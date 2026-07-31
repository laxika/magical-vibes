package com.github.laxika.magicalvibes.cards.d;

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

@CardRegistration(set = "M14", collectorNumber = "137")
public class DragonEgg extends Card {

    public DragonEgg() {
        // When this creature dies, create a 2/2 red Dragon creature token with flying
        // and "{R}: This token gets +1/+0 until end of turn."
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
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
        ));
    }
}
