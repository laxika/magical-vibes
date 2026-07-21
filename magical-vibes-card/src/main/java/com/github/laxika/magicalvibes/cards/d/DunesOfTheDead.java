package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "175")
public class DunesOfTheDead extends Card {

    public DunesOfTheDead() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // When this land is put into a graveyard from the battlefield, create a 2/2 black Zombie
        // creature token.
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                CreateTokenEffect.blackZombie(1));
    }
}
