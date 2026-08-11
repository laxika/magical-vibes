package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "275")
public class StillLife extends Card {

    public StillLife() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}",
                List.of(new AnimatePermanentsEffect(4, 3, List.of(CardSubtype.CENTAUR), Set.of())),
                "{G}{G}: This enchantment becomes a 4/3 Centaur creature in addition to its other types until end of turn."
        ));
    }
}
