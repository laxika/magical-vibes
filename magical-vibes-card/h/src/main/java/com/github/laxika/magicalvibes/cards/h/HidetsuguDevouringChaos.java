package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "99")
public class HidetsuguDevouringChaos extends Card {

    public HidetsuguDevouringChaos() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeCreatureCost(), new ScryEffect(2)),
                "{B}, Sacrifice a creature: Scry 2."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffect()),
                "{2}{R}, {T}: Exile the top card of your library. You may play that card this turn. "
                        + "When you exile a nonland card this way, Hidetsugu deals damage equal to the "
                        + "exiled card's mana value to any target."
        ));
    }
}
