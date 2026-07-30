package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "181")
public class JadeMage extends Card {

    public JadeMage() {
        // {2}{G}: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new CreateTokenEffect(1, "Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{2}{G}: Create a 1/1 green Saproling creature token."
        ));
    }
}
