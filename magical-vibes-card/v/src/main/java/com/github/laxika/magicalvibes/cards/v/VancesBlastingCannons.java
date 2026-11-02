package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SpitfireBastion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ExileTopCardsMayCastMatchingThisTurnEffect(
                        1, new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

        // Whenever you cast your third spell in a turn, you may transform
        // Vance's Blasting Cannons.
        addTrigger(Triggers.youCastSpell(),
                new NthSpellCastTriggerEffect(3, List.of(
                        new MayEffect(new TransformSelfEffect(),
                                "Transform Vance's Blasting Cannons?"))));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpitfireBastion";
    }
}
