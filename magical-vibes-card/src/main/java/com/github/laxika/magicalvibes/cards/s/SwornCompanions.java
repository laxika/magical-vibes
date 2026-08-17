package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "27")
public class SwornCompanions extends Card {

    public SwornCompanions() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SOLDIER), Set.of(Keyword.LIFELINK), Set.of()
        ));
    }
}
