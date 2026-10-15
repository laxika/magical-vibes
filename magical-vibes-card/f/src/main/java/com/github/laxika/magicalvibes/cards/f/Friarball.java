package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CoststormEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "278")
@CardRegistration(set = "MB2", collectorNumber = "514")
public class Friarball extends Card {

    public Friarball() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Monk", 2, 2, CardColor.WHITE, List.of(CardSubtype.MONK), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_SELF_CAST, new CoststormEffect());
    }
}
