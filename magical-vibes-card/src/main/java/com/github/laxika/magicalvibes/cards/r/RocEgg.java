package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "25")
public class RocEgg extends Card {

    public RocEgg() {
        // When Roc Egg dies, create a 3/3 white Bird creature token with flying.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                "Bird", 3, 3, CardColor.WHITE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()
        ));
    }
}
