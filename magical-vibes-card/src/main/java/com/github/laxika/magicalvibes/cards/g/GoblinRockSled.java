package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "4ED", collectorNumber = "199")
public class GoblinRockSled extends Card {

    public GoblinRockSled() {
        // This creature doesn't untap during your untap step if it attacked during your last turn.
        addEffect(EffectSlot.ON_ATTACK, new SkipNextUntapEffect(TapUntapScope.SELF));

        // This creature can't attack unless defending player controls a Mountain.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                "a Mountain"
        ));
    }
}
