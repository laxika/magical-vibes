package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "324")
@CardRegistration(set = "TPR", collectorNumber = "242")
@CardRegistration(set = "VMA", collectorNumber = "310")
public class SaltFlats extends Card {

    public SaltFlats() {
        // This land enters tapped.
        addStaticEffect(new EntersTappedEffect());
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Add {W}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), DealDamageEffect.to(Subjects.you(), 1)),
                "{T}: Add {W}. Salt Flats deals 1 damage to you."
        ));
        // {T}: Add {B}. This land deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), DealDamageEffect.to(Subjects.you(), 1)),
                "{T}: Add {B}. Salt Flats deals 1 damage to you."
        ));
    }
}
