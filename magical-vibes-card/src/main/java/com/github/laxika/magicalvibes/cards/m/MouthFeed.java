package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.Feed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

/**
 * Mouth // Feed — front half (Mouth).
 * Sorcery — Create a 3/3 green Hippo creature token.
 * Back half (Feed) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "214")
public class MouthFeed extends Card {

    public MouthFeed() {
        setBackFaceCard(new Feed());

        // Create a 3/3 green Hippo creature token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Hippo", 3, 3,
                CardColor.GREEN, List.of(CardSubtype.HIPPO), Set.of(), Set.of()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Feed";
    }
}
