package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "134")
public class HowlsquadHeavy extends Card {

    public HowlsquadHeavy() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new CreateTokenEffect(
                1,
                "Goblin",
                1,
                1,
                CardColor.RED,
                null,
                List.of(CardSubtype.GOBLIN),
                Set.of(),
                Set.of()));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MakeCreatedPermanentsMustAttackThisCombatEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED,
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN), CountScope.CONTROLLER))),
                "Max speed — {T}: Add {R} for each Goblin you control."
        ).withActivationCondition(new MaxSpeed(), "Activate only if you have max speed"));
    }
}
