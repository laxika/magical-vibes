package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.Finish;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

/**
 * Start // Finish — front half (Start).
 * Instant — Create two 1/1 white Warrior creature tokens with vigilance.
 * Back half (Finish) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "215")
public class StartFinish extends Card {

    public StartFinish() {
        setBackFaceCard(new Finish());

        // Create two 1/1 white Warrior creature tokens with vigilance.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.WARRIOR), Set.of(Keyword.VIGILANCE), Set.of()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Finish";
    }
}
