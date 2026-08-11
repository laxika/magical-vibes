package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameRevealTopCardEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "225")
public class DivinersLockbox extends Card {

    public DivinersLockbox() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ChooseCardNameRevealTopCardEffect()),
                "{1}, {T}: Choose a card name, then reveal the top card of your library. If that card has the chosen name, sacrifice this artifact and draw three cards. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
