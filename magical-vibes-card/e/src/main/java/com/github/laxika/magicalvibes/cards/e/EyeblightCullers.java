package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHC", collectorNumber = "48")
public class EyeblightCullers extends Card {

    public EyeblightCullers() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new CreateTokenEffect(3, "Elf Warrior", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()),
                new MillEffect(3, MillRecipient.CONTROLLER)));
    }
}
