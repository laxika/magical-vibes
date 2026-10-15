package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "66")
public class MerfolkMesmerist extends Card {

    public MerfolkMesmerist() {
        // {U}, {T}: Target player mills two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER)),
                "{U}, {T}: Target player mills two cards."
        ));
    }
}
