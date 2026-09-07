package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "76")
@CardRegistration(set = "RAV", collectorNumber = "74")
public class VedalkenEntrancer extends Card {

    public VedalkenEntrancer() {
        // {U}, {T}: Target player mills two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER)),
                "{U}, {T}: Target player mills two cards."
        ));
    }
}
