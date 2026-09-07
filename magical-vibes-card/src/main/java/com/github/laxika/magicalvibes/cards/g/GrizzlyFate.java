package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JUD", collectorNumber = "119")
public class GrizzlyFate extends Card {

    public GrizzlyFate() {
        CreateTokenEffect twoBears = new CreateTokenEffect(2, "Bear", 2, 2,
                CardColor.GREEN, List.of(CardSubtype.BEAR), Set.of(), Set.of());
        CreateTokenEffect fourBears = new CreateTokenEffect(4, "Bear", 2, 2,
                CardColor.GREEN, List.of(CardSubtype.BEAR), Set.of(), Set.of());
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GraveyardCardThreshold(7, null), twoBears, fourBears));
        addCastingOption(new FlashbackCast("{5}{G}{G}"));
    }
}
