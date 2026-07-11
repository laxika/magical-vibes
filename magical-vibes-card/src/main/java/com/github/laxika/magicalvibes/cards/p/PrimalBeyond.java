package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorSubtypeSpellOrAbilityManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "149")
public class PrimalBeyond extends Card {

    public PrimalBeyond() {
        // As this land enters, you may reveal an Elemental card from your hand.
        // If you don't, this land enters tapped.
        addEffect(EffectSlot.STATIC, new RevealSubtypeOrEntersTappedEffect(CardSubtype.ELEMENTAL));

        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {T}: Add one mana of any color. Spend this mana only to cast an Elemental spell
        // or activate an ability of an Elemental.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorSubtypeSpellOrAbilityManaEffect(1, CardSubtype.ELEMENTAL)),
                "{T}: Add one mana of any color. Spend this mana only to cast an Elemental spell or activate an ability of an Elemental."
        ));
    }
}
