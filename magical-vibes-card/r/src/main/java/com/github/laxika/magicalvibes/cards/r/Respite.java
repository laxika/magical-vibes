package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "TMP", collectorNumber = "249")
public class Respite extends Card {

    public Respite() {
        addSpellEffect(PreventDamageEffect.allCombat());
        addSpellEffect(new GainLifeEffect(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER)));
    }
}
