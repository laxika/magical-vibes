package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "121")
@CardRegistration(set = "CMM", collectorNumber = "496")
public class StitcherGeralf extends Card {

    public StitcherGeralf() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(new MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect(
                        new CreateTokenEffect("Zombie", 0, 0, CardColor.BLUE,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()))),
                "{2}{U}, {T}: Each player mills three cards. Exile up to two creature cards put into "
                        + "graveyards this way. Create an X/X blue Zombie creature token, where X is "
                        + "the total power of the cards exiled this way."
        ));
    }
}
