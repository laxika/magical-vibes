package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "164")
public class ForceOfVigor extends Card {

    public ForceOfVigor() {
        // If it's not your turn, you may exile a green card from your hand rather than pay this
        // spell's mana cost.
        addCastingOption(new AlternateHandCast(
                List.of(new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.GREEN), "green")),
                new NotControllerTurn(),
                false));

        // Destroy up to two target artifacts and/or enchantments.
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Targets must be artifacts and/or enchantments"
        ), 0, 2).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
