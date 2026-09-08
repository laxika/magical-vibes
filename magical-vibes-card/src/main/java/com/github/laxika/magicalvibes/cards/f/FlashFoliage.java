package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.CreateTokenBlockingCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "85")
public class FlashFoliage extends Card {

    public FlashFoliage() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.COMBAT_AFTER_BLOCKERS);

        target(new PermanentPredicateTargetFilter(
                        new PermanentIsAttackingSourceControllerPredicate(),
                        "Target must be a creature attacking you"))
                .addEffect(EffectSlot.SPELL, new CreateTokenBlockingCombatOpponentEffect(
                        new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
