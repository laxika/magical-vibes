package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "19")
@CardRegistration(set = "LTC", collectorNumber = "102")
public class CorsairsOfUmbar extends Card {

    public CorsairsOfUmbar() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new AmassGoblinsEffect(3, CardSubtype.ORC));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{2}{U}: Target Goblin, Orc, or Pirate can't be blocked this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasAnySubtypePredicate(Set.of(
                                        CardSubtype.GOBLIN, CardSubtype.ORC, CardSubtype.PIRATE)))),
                        "Target must be a Goblin, Orc, or Pirate creature")));
    }
}
