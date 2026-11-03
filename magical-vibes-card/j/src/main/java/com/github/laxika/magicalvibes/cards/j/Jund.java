package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDevourToCastSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OPC2", collectorNumber = "20")
@CardRegistration(set = "OPCA", collectorNumber = "41")
@CardRegistration(set = "MOC", collectorNumber = "148")
public class Jund extends Card {

    public Jund() {
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardAnyOfPredicate(List.of(
                                        new CardColorPredicate(CardColor.BLACK),
                                        new CardColorPredicate(CardColor.RED),
                                        new CardColorPredicate(CardColor.GREEN)))))),
                new GrantDevourToCastSpellEffect(5));
        addTrigger(Triggers.chaosEnsues(),
                new CreateTokenEffect(2, "Goblin", 1, 1, CardColor.RED,
                        List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
