package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextSpellOfTypeThisTurnEffect;

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
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.SPELL, new GrantFlashToNextSpellOfTypeThisTurnEffect(CardType.CREATURE));
        addEffect(EffectSlot.SPELL, new EmpowerNextCreatureSpellThisTurnEffect(true, 1));
    }
}
