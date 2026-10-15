package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "47")
public class LuSuWuAdvisor extends Card {

    public LuSuWuAdvisor() {
        // {T}: Draw a card. Activate only during your turn, before attackers are declared.
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(1)),
                "{T}: Draw a card. Activate only during your turn, before attackers are declared.",
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED));
    }
}
