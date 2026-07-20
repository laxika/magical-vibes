package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeToCastEffect;

import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "192")
public class VizierOfTheMenagerie extends Card {

    public VizierOfTheMenagerie() {
        // "You may look at the top card of your library any time."
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        // "You may cast creature spells from the top of your library."
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE)));
        // "You can spend mana of any type to cast creature spells."
        addEffect(EffectSlot.STATIC, new SpendAnyManaTypeToCastEffect(Set.of(CardType.CREATURE)));
    }
}
