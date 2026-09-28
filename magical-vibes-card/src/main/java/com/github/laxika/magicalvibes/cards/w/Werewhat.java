package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.WerewhatOnEnterEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "349")
@CardRegistration(set = "MB2", collectorNumber = "351")
@CardRegistration(set = "MB2", collectorNumber = "587")
public class Werewhat extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("Werewhat", new OracleData(
                "Werewhat",
                CardType.CREATURE,
                Set.of(),
                "{3}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.WEREWOLF),
                "Daybound\n"
                        + "As Werewhat enters the battlefield, you may exile a creature card from your graveyard or hand. "
                        + "If you do, that card becomes this creature's back face and that back face has nightbound. "
                        + "If you exiled a card from your hand this way, draw a card.",
                4,
                3,
                Set.of(Keyword.DAYBOUND),
                null,
                null,
                null));
    }

    public Werewhat() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new WerewhatOnEnterEffect());
    }
}
