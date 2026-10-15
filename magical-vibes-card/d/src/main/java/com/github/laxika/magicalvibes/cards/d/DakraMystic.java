package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardMayGraveyardOrDrawEffect;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "35")
public class DakraMystic extends Card {

    public DakraMystic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new EachPlayerRevealsTopCardMayGraveyardOrDrawEffect()),
                "{U}, {T}: Each player reveals the top card of their library. You may put the revealed cards into their owners' graveyards. If you don't, each player draws a card."
        ));
    }
}
