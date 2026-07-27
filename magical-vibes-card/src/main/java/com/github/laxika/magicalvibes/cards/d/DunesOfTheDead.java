package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;


@CardRegistration(set = "HOU", collectorNumber = "175")
public class DunesOfTheDead extends Card {

    public DunesOfTheDead() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // When this land is put into a graveyard from the battlefield, create a 2/2 black Zombie
        // creature token.
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                CreateTokenEffect.blackZombie(1));
    }
}
