package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TokensCreatedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "306")
public class ThalisseReverentMedium extends Card {

    public ThalisseReverentMedium() {
        // At the beginning of each end step, create a 1/1 white Spirit creature token with flying
        // for each token you created this turn.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new CreateTokenEffect(
                new TokensCreatedThisTurn(), "Spirit", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));
    }
}
