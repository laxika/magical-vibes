package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutLandFromHandThenOpponentsDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "350")
@CardRegistration(set = "MB2", collectorNumber = "589")
public class AnaxAndCymedeKynaiosAndTiro extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("AnaxAndCymedeKynaiosAndTiro", new OracleData(
                "Anax and Cymede & Kynaios and Tiro",
                CardType.CREATURE,
                Set.of(),
                "{1}{R}{G}{W}{U}",
                CardColor.WHITE,
                List.of(CardColor.WHITE, CardColor.BLUE, CardColor.RED, CardColor.GREEN),
                List.of(CardColor.WHITE, CardColor.BLUE, CardColor.RED, CardColor.GREEN),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                "First strike, vigilance\n"
                        + "Heroic — Whenever you cast a spell that targets Anax and Cymede & Kynaios and Tiro, draw a card. "
                        + "Each player may put a land card from their hand onto the battlefield, then each opponent who didn't draws a card.",
                3,
                8,
                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE),
                null,
                null,
                null));
    }

    public AnaxAndCymedeKynaiosAndTiro() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(
                        new DrawCardEffect(),
                        new EachPlayerMayPutLandFromHandThenOpponentsDrawEffect()),
                new StackEntryTargetsSourcePredicate()));
    }
}
