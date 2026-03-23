package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rite of Belzenlok — {2}{B}{B} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Create two 0/1 black Cleric creature tokens.
 * III — Create a 6/6 black Demon creature token with flying, trample, and "At the beginning
 *        of your upkeep, sacrifice another creature. If you can't, this creature deals 6
 *        damage to you."
 */
@CardRegistration(set = "DOM", collectorNumber = "102")
public class RiteOfBelzenlok extends Card {

    public RiteOfBelzenlok() {
        // Chapter I: Create two 0/1 black Cleric creature tokens
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                2, "Cleric", 0, 1, CardColor.BLACK,
                List.of(CardSubtype.CLERIC),
                Set.of(), Set.of()
        ));

        // Chapter II: Same as chapter I
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                2, "Cleric", 0, 1, CardColor.BLACK,
                List.of(CardSubtype.CLERIC),
                Set.of(), Set.of()
        ));

        // Chapter III: Create a 6/6 black Demon creature token with flying, trample,
        // and "At the beginning of your upkeep, sacrifice another creature. If you can't,
        // this creature deals 6 damage to you."
        addEffect(EffectSlot.SAGA_CHAPTER_III, new CreateTokenEffect(
                1, "Demon", 6, 6, CardColor.BLACK,
                List.of(CardSubtype.DEMON),
                Set.of(Keyword.FLYING, Keyword.TRAMPLE), Set.of(),
                Map.of(EffectSlot.UPKEEP_TRIGGERED, new SacrificeOtherCreatureOrDamageEffect(6))
        ));
    }
}
