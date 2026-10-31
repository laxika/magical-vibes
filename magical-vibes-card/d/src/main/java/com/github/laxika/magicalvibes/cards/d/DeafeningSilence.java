package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ELD", collectorNumber = "10")
@CardRegistration(set = "SLD", collectorNumber = "1219")
@CardRegistration(set = "SPG", collectorNumber = "120")
@CardRegistration(set = "MB2", collectorNumber = "9")
public class DeafeningSilence extends Card {

    public DeafeningSilence() {
        addStaticEffect(new LimitSpellsPerTurnEffect(1, SpellLimitScope.EACH_PLAYER,
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
    }
}
