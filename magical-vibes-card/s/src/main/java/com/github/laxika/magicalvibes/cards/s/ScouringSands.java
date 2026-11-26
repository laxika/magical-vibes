package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BNG", collectorNumber = "110")
public class ScouringSands extends Card {

    public ScouringSands() {
        addSpellEffect(DealDamageEffect.toEachCreature(1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addSpellEffect(new ScryEffect(1));
    }
}
