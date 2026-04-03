package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardFlashbackOnlyAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "144")
public class AltarOfTheLost extends Card {

    public AltarOfTheLost() {
        // Altar of the Lost enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add two mana in any combination of colors. Spend this mana only to cast spells with flashback from a graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardFlashbackOnlyAnyColorManaEffect(2)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast spells with flashback from a graveyard."
        ));
    }
}
