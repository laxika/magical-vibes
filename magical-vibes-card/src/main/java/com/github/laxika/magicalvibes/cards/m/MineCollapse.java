package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "135")
public class MineCollapse extends Card {

    public MineCollapse() {
        // If it's your turn, you may sacrifice a Mountain rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(
                List.of(new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN))),
                new ControllerTurn(), false));

        // Mine Collapse deals 5 damage to target creature or planeswalker.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureOrPlaneswalkerEffect(5));
    }
}
