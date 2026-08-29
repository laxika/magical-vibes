package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BNG", collectorNumber = "110")
public class ScouringSands extends Card {

    public ScouringSands() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(1, false, false,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
