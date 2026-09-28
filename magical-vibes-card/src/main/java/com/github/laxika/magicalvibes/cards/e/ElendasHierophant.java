package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "39")
@CardRegistration(set = "LCC", collectorNumber = "71")
public class ElendasHierophant extends Card {

    public ElendasHierophant() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCountersOnSourceEffect(1, 1, 1));

        // When Elenda's Hierophant dies, create Vampire tokens equal to its last-known power.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                new SourcePower(),
                "Vampire",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.VAMPIRE),
                Set.of(Keyword.LIFELINK),
                Set.of()
        ));
    }
}
