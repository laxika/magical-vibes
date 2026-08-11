package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "28")
public class Personify extends Card {

    public Personify() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, FlickerEffect.flickerTarget())
                .addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                        "Shapeshifter", 1, 1, null,
                        List.of(CardSubtype.SHAPESHIFTER),
                        Set.of(Keyword.CHANGELING), Set.of()));
    }
}
