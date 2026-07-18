package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "6ED", collectorNumber = "1")
@CardRegistration(set = "5ED", collectorNumber = "5")
@CardRegistration(set = "4ED", collectorNumber = "4")
public class AnimateWall extends Card {

    public AnimateWall() {
        // Enchant Wall
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.WALL),
                "Target must be a Wall"
        ))
        // Enchanted Wall can attack as though it didn't have defender.
        // Read from the enchanted creature by GameQueryService.canAttackDespiteDefender via hasAuraWithEffect.
        .addEffect(EffectSlot.STATIC, new CanAttackAsThoughNoDefenderEffect());
    }
}
