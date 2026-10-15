package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextInstantOrSorceryDamageToControllerEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "1")
public class AegisOfHonor extends Card {

    public AegisOfHonor() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RedirectNextInstantOrSorceryDamageToControllerEffect()),
                "{1}: The next time an instant or sorcery spell would deal damage to you this turn, that spell deals that damage to its controller instead."));
    }
}
