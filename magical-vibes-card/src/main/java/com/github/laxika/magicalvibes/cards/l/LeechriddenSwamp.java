package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "273")
public class LeechriddenSwamp extends Card {

    public LeechriddenSwamp() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK)),
                "{T}: Add {B}."
        ));

        // {B}, {T}: Each opponent loses 1 life. Activate only if you control two or more black permanents.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)),
                "{B}, {T}: Each opponent loses 1 life. Activate only if you control two or more black permanents."
        ).withRequiredControlledPermanents(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)), 2, "black permanents"));
    }
}
