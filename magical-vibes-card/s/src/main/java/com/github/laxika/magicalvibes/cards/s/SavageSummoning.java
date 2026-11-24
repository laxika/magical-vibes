package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Savage Summoning — {G} instant.
 *
 * <p>"This spell can't be countered. The next creature spell you cast this turn can be cast as
 * though it had flash. That spell can't be countered. That creature enters with an additional
 * +1/+1 counter on it."</p>
 */
@CardRegistration(set = "M14", collectorNumber = "194")
public class SavageSummoning extends Card {

    public SavageSummoning() {
        addStaticEffect(new CantBeCounteredEffect());
        addSpellEffect(new GrantFlashToNextMatchingSpellEffect(new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new EmpowerNextCreatureSpellThisTurnEffect(true, 1));
    }
}
