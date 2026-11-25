package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "142")
public class RhysticCave extends Card {

    public RhysticCave() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ChooseColorAtResolutionEffect(),
                        UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{1}", new AwardManaOfChosenColorEffect())
                                .ifPaid(new ClearChosenColorEffect())
                                .withPrompt("Pay {1} to prevent Rhystic Cave from producing mana?")),
                "{T}: Choose a color. Add one mana of that color unless any player pays {1}. Activate only as an instant."
        ));
    }
}
