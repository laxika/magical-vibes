package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "266")
public class NullstoneGargoyle extends Card {

    public NullstoneGargoyle() {
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                        .nthThisTurn(1)
                        .countingEveryPlayersSpells(),
                new CounterSpellEffect());
    }
}
