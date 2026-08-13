package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillTwoRepeatIfSharedColorEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "290")
@CardRegistration(set = "TPR", collectorNumber = "223")
public class Grindstone extends Card {

    public Grindstone() {
        // {3}, {T}: Target player mills two cards. If two cards that share a color were milled this
        // way, repeat this process.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MillTwoRepeatIfSharedColorEffect()),
                "{3}, {T}: Target player mills two cards. If two cards that share a color were "
                        + "milled this way, repeat this process."
        ));
    }
}
