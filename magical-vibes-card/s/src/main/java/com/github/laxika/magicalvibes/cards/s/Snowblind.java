package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.IfSourceAttacking;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ShrinkEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "264")
public class Snowblind extends Card {

    public Snowblind() {
        PermanentPredicate snowLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.SNOW)));

        // Enchant creature
        // Enchanted creature gets -X/-Y. If that creature is attacking, X is the number of snow
        // lands defending player controls. Otherwise, X is the number of snow lands its controller
        // controls. Y is equal to X or to enchanted creature's toughness minus 1, whichever is
        // smaller. The amount is evaluated from the enchanted creature's perspective, so
        // IfSourceAttacking reads its combat state; the Y clamp lives in the effect's handler.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ShrinkEnchantedCreatureEffect(new IfSourceAttacking(
                        new PermanentCount(snowLand, CountScope.DEFENDING_PLAYER),
                        new PermanentCount(snowLand, CountScope.CONTROLLER))));
    }
}
