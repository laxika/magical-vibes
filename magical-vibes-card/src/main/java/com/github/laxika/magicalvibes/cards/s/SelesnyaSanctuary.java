package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "281")
public class SelesnyaSanctuary extends Card {

    public SelesnyaSanctuary() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // When this land enters, return a land you control to its owner's hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentIsLandPredicate(),
                "land"
        ));

        // {T}: Add {G}{W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {G}{W}."
        ));
    }
}
