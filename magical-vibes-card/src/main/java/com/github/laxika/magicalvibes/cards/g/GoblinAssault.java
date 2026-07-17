package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "101")
public class GoblinAssault extends Card {

    public GoblinAssault() {
        // At the beginning of your upkeep, create a 1/1 red Goblin creature token with haste.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new CreateTokenEffect(
                "Goblin",
                1,
                1,
                CardColor.RED,
                List.of(CardSubtype.GOBLIN),
                Set.of(Keyword.HASTE),
                Set.of()
        ));

        // Goblin creatures attack each combat if able (all Goblins, any controller).
        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
    }
}
