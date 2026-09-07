package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "242")
@CardRegistration(set = "INR", collectorNumber = "432")
@CardRegistration(set = "MID", collectorNumber = "229")
public class JoinTheDance extends Card {

    public JoinTheDance() {
        // Create two 1/1 white Human creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Human", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));
        // Flashback {3}{G}{W}
        addCastingOption(new FlashbackCast("{3}{G}{W}"));
    }
}
