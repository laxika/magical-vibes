package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "203")
public class ThornLieutenant extends Card {

    public ThornLieutenant() {
        // Whenever this creature becomes the target of a spell or ability an opponent controls,
        // create a 1/1 green Elf Warrior creature token.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CreateTokenEffect("Elf Warrior", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()));

        // {5}{G}: This creature gets +4/+4 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{5}{G}", List.of(new BoostSelfEffect(4, 4)),
                "{5}{G}: This creature gets +4/+4 until end of turn."));
    }
}
