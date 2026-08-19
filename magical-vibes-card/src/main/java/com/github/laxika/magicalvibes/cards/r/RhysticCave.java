package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "142")
public class RhysticCave extends Card {

    public RhysticCave() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ChooseColorAtResolutionEffect(),
                        new MayPayManaEffect(
                                "{1}",
                                new ClearChosenColorEffect(),
                                "Pay {1} to prevent Rhystic Cave from producing mana?",
                                MayPayPayer.ANY_PLAYER,
                                new AwardManaOfChosenColorEffect(),
                                0)),
                "{T}: Choose a color. Add one mana of that color unless any player pays {1}. Activate only as an instant."
        ));
    }
}
