package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "71")
public class RakkaMar extends Card {

    public RakkaMar() {
        addActivatedAbility(new ActivatedAbility(true, "{R}", List.of(new CreateTokenEffect("Elemental", 3, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), Set.of())), "{R}, {T}: Create a 3/1 red Elemental creature token with haste."));
    }
}
