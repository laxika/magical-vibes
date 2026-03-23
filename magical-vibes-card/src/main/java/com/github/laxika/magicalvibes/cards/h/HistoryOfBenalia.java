package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

/**
 * History of Benalia — {1}{W}{W} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Create a 2/2 white Knight creature token with vigilance.
 * III — Knights you control get +2/+1 until end of turn.
 */
@CardRegistration(set = "DOM", collectorNumber = "21")
public class HistoryOfBenalia extends Card {

    public HistoryOfBenalia() {
        // Chapter I: Create a 2/2 white Knight creature token with vigilance
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE), Set.of()
        ));

        // Chapter II: Same as chapter I
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE), Set.of()
        ));

        // Chapter III: Knights you control get +2/+1 until end of turn
        addEffect(EffectSlot.SAGA_CHAPTER_III, new BoostAllOwnCreaturesEffect(
                2, 1, new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)
        ));
    }
}
