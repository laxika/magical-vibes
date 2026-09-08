package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "252")
public class SelesnyaGuildmage extends Card {

    public SelesnyaGuildmage() {
        // {3}{G}: Create a 1/1 green Saproling creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{3}{G}: Create a 1/1 green Saproling creature token."
        ));

        // {3}{W}: Creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{3}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
