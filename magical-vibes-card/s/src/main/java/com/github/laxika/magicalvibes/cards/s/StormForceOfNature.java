package com.github.laxika.magicalvibes.cards.s;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantAbilityToNextSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1742")
@CardRegistration(set = "MAR", collectorNumber = "94")
public class StormForceOfNature extends Card {

    public StormForceOfNature() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                GrantAbilityToNextSpellThisTurnEffect.storm(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))));
    }
}
