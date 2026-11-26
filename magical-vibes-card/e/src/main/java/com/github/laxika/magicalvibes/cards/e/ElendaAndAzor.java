package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "6")
@CardRegistration(set = "MOC", collectorNumber = "90")
public class ElendaAndAzor extends Card {

    public ElendaAndAzor() {
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPayX("{X}{W}{U}{B}", new DrawCardEffect(new XValue())));
        addTrigger(Triggers.endStep(ActivePlayer.ANY), UnlessPaysEffect.youMayPayLife(4, new CreateTokenEffect(
                        new CardsDrawnThisTurn(), "Vampire Knight", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.VAMPIRE, CardSubtype.KNIGHT), Set.of(Keyword.LIFELINK), Set.of())).withPrompt("Pay 4 life to create Vampire Knight tokens?"));
    }
}
