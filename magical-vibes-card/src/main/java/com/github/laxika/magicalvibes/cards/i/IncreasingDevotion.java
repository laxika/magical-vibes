package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CastFromZoneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "11")
public class IncreasingDevotion extends Card {

    public IncreasingDevotion() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(5, "Human", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new CastFromZoneConditionalEffect(Zone.GRAVEYARD,
                new CreateTokenEffect(5, "Human", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of())));
        addCastingOption(new FlashbackCast("{7}{W}{W}"));
    }
}
