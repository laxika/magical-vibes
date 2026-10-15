package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SpitfireBastion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Vance's Blasting Cannons — front face of Vance's Blasting Cannons // Spitfire Bastion.
 * {3}{R} Legendary Enchantment.
 * At the beginning of your upkeep, exile the top card of your library.
 * If it's a nonland card, you may cast that card this turn.
 * Whenever you cast your third spell in a turn, you may transform Vance's Blasting Cannons.
 */
@CardRegistration(set = "XLN", collectorNumber = "173")
public class VancesBlastingCannons extends Card {

    public VancesBlastingCannons() {
        setBackFaceCard(new SpitfireBastion());

        // At the beginning of your upkeep, exile the top card of your library.
        // If it's a nonland card, you may cast that card this turn.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileTopCardsMayCastMatchingThisTurnEffect(
                        1, new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

        // Whenever you cast your third spell in a turn, you may transform
        // Vance's Blasting Cannons.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(
                        new NthSpellCastTriggerEffect(3, List.of(new TransformSelfEffect())),
                        "Transform Vance's Blasting Cannons?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpitfireBastion";
    }
}
