package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "87")
@CardRegistration(set = "DDS", collectorNumber = "24")
@CardRegistration(set = "PIO", collectorNumber = "234")
public class NivixCyclops extends Card {

    public NivixCyclops() {
        // Whenever you cast an instant or sorcery spell, this creature gets +3/+0 until end of turn
        // and can attack this turn as though it didn't have defender.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        ))),
                new BoostSelfEffect(3, 0),
                new CanAttackAsThoughNoDefenderEffect());
    }
}
