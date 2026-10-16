package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "196")
public class KumenasSpeaker extends Card {

    public KumenasSpeaker() {
        // This creature gets +1/+1 as long as you control another Merfolk or an Island.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnyOf(List.of(
                        new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)))),
                new StaticBoostEffect(1, 1, GrantScope.SELF)));
    }
}
