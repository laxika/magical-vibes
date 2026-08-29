package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "146")
public class AthreosGodOfPassage extends Card {

    public AthreosGodOfPassage() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(Set.of(ManaColor.WHITE, ManaColor.BLACK), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                new ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(3));
    }
}
