package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerLostGameThisMatch;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MB1", collectorNumber = "10")
public class RuffUnderdogChamp extends Card {

    public RuffUnderdogChamp() {
        // All Hounds are Dogs.
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.DOG,
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                false,
                new PermanentHasSubtypePredicate(CardSubtype.HOUND)));

        // Underdog - if the controller has lost a game this match, Ruff and other Dogs get +1/+1.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerLostGameThisMatch(),
                new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.DOG))));
    }
}
