package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "53")
public class DragonBroodmother extends Card {

    public DragonBroodmother() {
        // At the beginning of each upkeep, create a 1/1 red and green Dragon creature token
        // with flying and devour 2 (a Devour 2 as-enters replacement on the token).
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Dragon",
                1,
                1,
                CardColor.RED,
                Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.DRAGON),
                Set.of(Keyword.FLYING),
                Set.of(),
                false,
                false,
                Map.<EffectSlot, CardEffect>of(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(2)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
    }
}
